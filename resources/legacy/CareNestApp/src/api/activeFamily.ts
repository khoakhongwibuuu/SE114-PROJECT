import AsyncStorage from '@react-native-async-storage/async-storage';

export const ACTIVE_FAMILY_STORAGE_KEY = 'carenest:activeFamilyId';

let activeFamilyIdCache: number | null | undefined;

function parseFamilyId(value: string | null): number | null {
  if (!value) {
    return null;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function setActiveFamilyHeaderId(familyId: number | null): void {
  activeFamilyIdCache = familyId;
}

export async function getActiveFamilyHeaderId(): Promise<number | null> {
  if (activeFamilyIdCache !== undefined) {
    return activeFamilyIdCache;
  }

  activeFamilyIdCache = parseFamilyId(await AsyncStorage.getItem(ACTIVE_FAMILY_STORAGE_KEY));
  return activeFamilyIdCache;
}

