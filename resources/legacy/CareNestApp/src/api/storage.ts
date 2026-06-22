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

  // Migrate legacy plain-text sessions once, then delete them.
  try {
    const fallbackData = await AsyncStorage.getItem(FALLBACK_SESSION_KEY);
    if (fallbackData) {
      const migratedSession = JSON.parse(fallbackData) as AuthSession;
      await Keychain.setGenericPassword('carenest_user', JSON.stringify(migratedSession), {
        service: KEYCHAIN_SERVICE,
        securityLevel: Keychain.SECURITY_LEVEL.SECURE_SOFTWARE,
      });
      await AsyncStorage.removeItem(FALLBACK_SESSION_KEY);
      inMemorySession = migratedSession;
      return inMemorySession;
    }
  } catch (fallbackError) {
    console.warn("Legacy AsyncStorage session migration failed:", fallbackError);
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

  try {
    await Keychain.setGenericPassword('carenest_user', JSON.stringify(session), {
      service: KEYCHAIN_SERVICE,
      securityLevel: Keychain.SECURITY_LEVEL.SECURE_SOFTWARE,
    });
    await AsyncStorage.removeItem(FALLBACK_SESSION_KEY);
  } catch (error) {
    console.warn("Keychain set session failed:", error);
    throw error;
  }
}

