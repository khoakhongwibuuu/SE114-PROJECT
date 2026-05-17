import axios, { AxiosError } from 'axios';
import { DeviceEventEmitter } from 'react-native';
import { API_BASE_URL } from './config';
import { getStoredSession, setStoredSession } from './storage';

export interface ApiEnvelope<T> {
  success: boolean;
  message: string;
  data: T;
  errors?: unknown;
  timestamp?: string;
}


export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 300000,
});

const DEFAULT_GET_CACHE_TTL_MS = 20000;

const getResponseCache = new Map<string, { data: unknown; expiresAt: number }>();
const inflightGetRequests = new Map<string, Promise<unknown>>();

function stableStringify(value: unknown): string {
  if (value === null || value === undefined) {
    return 'null';
  }

  if (typeof value !== 'object') {
    return JSON.stringify(value);
  }

  if (Array.isArray(value)) {
    return `[${value.map(stableStringify).join(',')}]`;
  }

  const entries = Object.entries(value as Record<string, unknown>).sort(([a], [b]) =>
    a.localeCompare(b),
  );
  return `{${entries
    .map(([key, val]) => `${JSON.stringify(key)}:${stableStringify(val)}`)
    .join(',')}}`;
}

function buildGetCacheKey(url: string, params?: Record<string, unknown>): string {
  if (!params || Object.keys(params).length === 0) {
    return `GET:${url}`;
  }

  return `GET:${url}?${stableStringify(params)}`;
}

function readFreshCache<T>(cacheKey: string): T | null {
  const cached = getResponseCache.get(cacheKey);
  if (!cached) {
    return null;
  }

  if (Date.now() > cached.expiresAt) {
    getResponseCache.delete(cacheKey);
    return null;
  }

  return cached.data as T;
}

export function invalidateApiGetCache(matchers?: Array<string | RegExp>): void {
  if (!matchers || matchers.length === 0) {
    getResponseCache.clear();
    inflightGetRequests.clear();
    return;
  }

  for (const cacheKey of Array.from(getResponseCache.keys())) {
    const matched = matchers.some(matcher =>
      typeof matcher === 'string' ? cacheKey.includes(matcher) : matcher.test(cacheKey),
    );

    if (matched) {
      getResponseCache.delete(cacheKey);
      inflightGetRequests.delete(cacheKey);
    }
  }
}

apiClient.interceptors.request.use(async config => {
  if (!config.headers.Accept) {
    config.headers.Accept = 'application/json';
  }

  const session = await getStoredSession();
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  response => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as any;

    // Handle 401 Unauthorized (Token refresh)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const session = await getStoredSession();
        if (session && session.refreshToken) {
          const { data } = await axios.post<{ data: { accessToken: string, refreshToken: string } }>(
            `${API_BASE_URL}/auth/refresh`,
            { refreshToken: session.refreshToken }
          );

          const newSession = {
            ...session,
            token: data.data.accessToken,
            refreshToken: data.data.refreshToken
          };
          await setStoredSession(newSession);

          originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`;
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh token failed, clear session
        await setStoredSession(null);
      }
    }

    // Handle 403 Forbidden (BOLA/IDOR protection kick)
    if (error.response?.status === 403) {
      console.warn('[API Client] 403 Forbidden detected. Emitting FAMILY_ACCESS_DENIED.');
      DeviceEventEmitter.emit('FAMILY_ACCESS_DENIED');
    }

    return Promise.reject(error);
  }
);

function extractApiError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message || error.message || 'Đã có lỗi xảy ra';
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Đã có lỗi xảy ra';
}

export async function apiGet<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  try {
    const response = await apiClient.get<ApiEnvelope<T>>(url, { params });
    return response.data.data;
  } catch (error) {
    throw new Error(extractApiError(error));
  }
}

export async function apiGetCached<T>(
  url: string,
  params?: Record<string, unknown>,
  options?: { ttlMs?: number; forceRefresh?: boolean },
): Promise<T> {
  const cacheKey = buildGetCacheKey(url, params);
  if (!options?.forceRefresh) {
    const cachedData = readFreshCache<T>(cacheKey);
    if (cachedData !== null) {
      return cachedData;
    }
  }

  const inflight = inflightGetRequests.get(cacheKey) as Promise<T> | undefined;
  if (inflight) {
    return inflight;
  }

  const request = apiGet<T>(url, params)
    .then(data => {
      const ttlMs = options?.ttlMs ?? DEFAULT_GET_CACHE_TTL_MS;
      if (ttlMs > 0) {
        getResponseCache.set(cacheKey, {
          data,
          expiresAt: Date.now() + ttlMs,
        });
      }
      return data;
    })
    .finally(() => {
      inflightGetRequests.delete(cacheKey);
    });

  inflightGetRequests.set(cacheKey, request as Promise<unknown>);
  return request;
}

export async function prefetchApiGet(
  url: string,
  params?: Record<string, unknown>,
  options?: { ttlMs?: number },
): Promise<void> {
  await apiGetCached(url, params, options).catch(() => undefined);
}

export async function apiPost<T, B = unknown>(url: string, body?: B, config?: Record<string, unknown>): Promise<T> {
  try {
    const response = await apiClient.post<ApiEnvelope<T>>(url, body, config);
    return response.data.data;
  } catch (error) {
    throw new Error(extractApiError(error));
  }
}

export async function apiPatch<T, B = unknown>(url: string, body?: B): Promise<T> {
  try {
    const response = await apiClient.patch<ApiEnvelope<T>>(url, body);
    return response.data.data;
  } catch (error) {
    throw new Error(extractApiError(error));
  }
}

export async function apiPut<T, B = unknown>(url: string, body?: B): Promise<T> {
  try {
    const response = await apiClient.put<ApiEnvelope<T>>(url, body);
    return response.data.data;
  } catch (error) {
    throw new Error(extractApiError(error));
  }
}

export async function apiDelete<T>(url: string): Promise<T> {
  try {
    const response = await apiClient.delete<ApiEnvelope<T>>(url);
    return response.data.data;
  } catch (error) {
    throw new Error(extractApiError(error));
  }
}
