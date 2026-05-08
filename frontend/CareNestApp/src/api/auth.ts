import { apiGet, apiGetCached, apiPatch, apiPost, apiClient, invalidateApiGetCache } from './client';
import type { AuthSession } from './storage';

function normalizeUploadUri(uri: string): string {
  if (!uri) {
    return uri;
  }

  if (
    uri.startsWith('file://')
    || uri.startsWith('content://')
    || uri.startsWith('ph://')
    || uri.startsWith('assets-library://')
  ) {
    return uri;
  }

  return `file://${uri}`;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  phoneNumber: string;
  password: string;
}


export interface CurrentUserProfile {
  id: number;
  email: string;
  fullName: string;
  phoneNumber?: string | null;
  birthday?: string | null;
  gender?: string | null;
  bloodType?: string | null;
  medicalHistory?: string | null;
  allergy?: string | null;
  height?: number | null;
  weight?: number | null;
  emergencyContactPhone?: string | null;
  avatarUrl?: string | null;
}


export interface UpdateCurrentUserProfilePayload {
  fullName: string;
  email: string;
  phoneNumber: string;
  birthday: string;
  gender: string;
  bloodType: string;
  medicalHistory?: string;
  allergy?: string;
  height: number;
  weight: number;
  emergencyContactPhone?: string;
}

export interface RawUserInfoResponse {
  id: number;
  email: string;
  fullName: string;
  phone?: string | null;
  dateOfBirth?: string | null;
  gender?: string | null;
  avatarUrl?: string | null;
  role?: string;
  isVerified?: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: RawUserInfoResponse;
}

function mapRawUserToProfile(raw: RawUserInfoResponse): CurrentUserProfile {
  return {
    id: raw.id,
    email: raw.email,
    fullName: raw.fullName,
    phoneNumber: raw.phone,
    birthday: raw.dateOfBirth,
    gender: raw.gender,
    avatarUrl: raw.avatarUrl,
  };
}


export async function login(payload: LoginPayload): Promise<AuthSession> {
  const data = await apiPost<AuthResponse, LoginPayload>('/auth/login', payload);
  return {
    token: data.accessToken,
    userId: data.user.id,
    email: data.user.email,
  };
}



export async function register(payload: RegisterPayload): Promise<void> {
  await apiPost('/auth/register', {
    ...payload,
  });
}



export async function forgotPassword(email: string): Promise<void> {
  await apiPost('/auth/forgot-password', { email });
}

export interface ResetPasswordPayload {
  email: string;
  otp: string;
  newPassword: string;
  confirmPassword: string;
}

export async function resetPassword(payload: ResetPasswordPayload): Promise<void> {
  await apiPost('/auth/reset-password', payload);
}

export async function getCurrentUserProfile(): Promise<CurrentUserProfile> {
  const raw = await apiGetCached<RawUserInfoResponse>('/auth/me', undefined, { ttlMs: 20000 });
  return mapRawUserToProfile(raw);
}



export async function updateCurrentUserProfile(payload: UpdateCurrentUserProfilePayload): Promise<CurrentUserProfile> {
  // MOCK: Backend has removed /auth/me update (moved to health-profiles).
  // This is a stub to prevent crashes in the UI.
  console.warn('updateCurrentUserProfile is deprecated on backend, mapping to a mocked response for now.');
  return {
    id: 0,
    email: payload.email,
    fullName: payload.fullName,
    phoneNumber: payload.phoneNumber,
    birthday: payload.birthday,
    gender: payload.gender,
    bloodType: payload.bloodType,
    medicalHistory: payload.medicalHistory,
    allergy: payload.allergy,
    height: payload.height,
    weight: payload.weight,
    emergencyContactPhone: payload.emergencyContactPhone,
  };
}

export async function changePassword(oldPassword: string, newPassword: string): Promise<void> {
  await apiPatch<void, { oldPassword: string; newPassword: string; confirmPassword: string }>('/users/change-password', {
    oldPassword,
    newPassword,
    confirmPassword: newPassword,
  });
}

export async function uploadAvatar(fileUri: string, fileName: string, mimeType: string): Promise<CurrentUserProfile> {
  const formData = new FormData();
  formData.append('avatar', {
    uri: normalizeUploadUri(fileUri),
    name: fileName,
    type: mimeType,
  } as unknown as Blob);

  const response = await apiClient.post<import('./client').ApiEnvelope<CurrentUserProfile>>('/auth/me/avatar', formData);
  invalidateApiGetCache(['/auth/me', '/dashboard', '/families/profiles/']);
  return response.data.data;
}

export async function getUsers(): Promise<Array<{ userId: number; email: string }>> {
  return apiGet<Array<{ userId: number; email: string }>>('/auth/users');
}
