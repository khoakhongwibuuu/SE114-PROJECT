import AsyncStorage from '@react-native-async-storage/async-storage';

const OFFLINE_CACHE_PREFIX = '@CareNest:offline-cache:';
const OFFLINE_CACHE_INDEX_KEY = '@CareNest:offline-cache:index';

type OfflineCacheEntry<T> = {
  data: T;
  savedAt: number;
};

function buildStorageKey(cacheKey: string): string {
  return `${OFFLINE_CACHE_PREFIX}${cacheKey}`;
}

async function readIndex(): Promise<string[]> {
  try {
    const raw = await AsyncStorage.getItem(OFFLINE_CACHE_INDEX_KEY);
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter(item => typeof item === 'string') : [];
  } catch (error) {
    console.warn('[OfflineCache] Không thể đọc danh sách cache:', error);
    return [];
  }
}

async function writeIndex(cacheKeys: string[]): Promise<void> {
  try {
    await AsyncStorage.setItem(OFFLINE_CACHE_INDEX_KEY, JSON.stringify(Array.from(new Set(cacheKeys))));
  } catch (error) {
    console.warn('[OfflineCache] Không thể lưu danh sách cache:', error);
  }
}

async function addToIndex(cacheKey: string): Promise<void> {
  const cacheKeys = await readIndex();
  if (!cacheKeys.includes(cacheKey)) {
    await writeIndex([...cacheKeys, cacheKey]);
  }
}

export async function saveOfflineCache<T>(cacheKey: string, data: T): Promise<void> {
  try {
    const entry: OfflineCacheEntry<T> = {
      data,
      savedAt: Date.now(),
    };
    await AsyncStorage.setItem(buildStorageKey(cacheKey), JSON.stringify(entry));
    await addToIndex(cacheKey);
  } catch (error) {
    console.warn('[OfflineCache] Không thể lưu cache ngoại tuyến:', error);
  }
}

export async function readOfflineCache<T>(
  cacheKey: string,
  maxAgeMs?: number,
): Promise<{ data: T; savedAt: number } | null> {
  try {
    const raw = await AsyncStorage.getItem(buildStorageKey(cacheKey));
    if (!raw) {
      return null;
    }

    const parsed = JSON.parse(raw) as OfflineCacheEntry<T>;
    if (!parsed || typeof parsed.savedAt !== 'number' || !('data' in parsed)) {
      await AsyncStorage.removeItem(buildStorageKey(cacheKey));
      return null;
    }

    if (maxAgeMs && Date.now() - parsed.savedAt > maxAgeMs) {
      return null;
    }

    return parsed;
  } catch (error) {
    console.warn('[OfflineCache] Không thể đọc cache ngoại tuyến:', error);
    return null;
  }
}

export async function removeOfflineCache(cacheKey: string): Promise<void> {
  try {
    await AsyncStorage.removeItem(buildStorageKey(cacheKey));
    const cacheKeys = await readIndex();
    await writeIndex(cacheKeys.filter(item => item !== cacheKey));
  } catch (error) {
    console.warn('[OfflineCache] Không thể xóa cache ngoại tuyến:', error);
  }
}

export async function removeOfflineCaches(matchers?: Array<string | RegExp>): Promise<void> {
  const cacheKeys = await readIndex();
  const matchedKeys = matchers && matchers.length > 0
    ? cacheKeys.filter(cacheKey =>
        matchers.some(matcher =>
          typeof matcher === 'string' ? cacheKey.includes(matcher) : matcher.test(cacheKey),
        ),
      )
    : cacheKeys;

  await Promise.all(matchedKeys.map(cacheKey => AsyncStorage.removeItem(buildStorageKey(cacheKey))));
  await writeIndex(cacheKeys.filter(cacheKey => !matchedKeys.includes(cacheKey)));
}
