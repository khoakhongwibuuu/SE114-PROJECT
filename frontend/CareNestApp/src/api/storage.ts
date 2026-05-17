import * as Keychain from 'react-native-keychain';
import AsyncStorage from '@react-native-async-storage/async-storage';

export interface AuthSession {
  token: string;
  refreshToken: string;
  userId: number;
  email: string;
}

const KEYCHAIN_SERVICE = 'com.carenest.session';
const FALLBACK_SESSION_KEY = '@carenest:session';

let inMemorySession: AuthSession | null = null;

export async function getStoredSession(): Promise<AuthSession | null> {
  if (inMemorySession) {
    return inMemorySession;
  }

  // 1. Try Keychain
  try {
    const credentials = await Keychain.getGenericPassword({ service: KEYCHAIN_SERVICE });
    if (credentials) {
      inMemorySession = JSON.parse(credentials.password) as AuthSession;
      return inMemorySession;
    }
  } catch (error) {
    console.warn("Keychain get session failed, trying fallback storage:", error);
  }

  // 2. Fallback to AsyncStorage
  try {
    const fallbackData = await AsyncStorage.getItem(FALLBACK_SESSION_KEY);
    if (fallbackData) {
      inMemorySession = JSON.parse(fallbackData) as AuthSession;
      return inMemorySession;
    }
  } catch (fallbackError) {
    console.error("Fallback AsyncStorage get session failed:", fallbackError);
  }

  return null;
}

export async function setStoredSession(session: AuthSession | null): Promise<void> {
  inMemorySession = session;
  
  if (!session) {
    try {
      await Keychain.resetGenericPassword({ service: KEYCHAIN_SERVICE });
    } catch (e) {
      console.warn("Keychain reset failed:", e);
    }
    try {
      await AsyncStorage.removeItem(FALLBACK_SESSION_KEY);
    } catch (e) {
      console.warn("AsyncStorage remove failed:", e);
    }
    return;
  }

  // 1. Store in AsyncStorage as the primary fail-safe layer
  try {
    await AsyncStorage.setItem(FALLBACK_SESSION_KEY, JSON.stringify(session));
  } catch (fallbackError) {
    console.error("AsyncStorage set session failed:", fallbackError);
  }

  // 2. Also try storing in Keychain for dual-layer security
  try {
    await Keychain.setGenericPassword('carenest_user', JSON.stringify(session), {
      service: KEYCHAIN_SERVICE,
      securityLevel: Keychain.SECURITY_LEVEL.SECURE_SOFTWARE,
    });
  } catch (error) {
    console.warn("Keychain set session failed:", error);
  }
}

