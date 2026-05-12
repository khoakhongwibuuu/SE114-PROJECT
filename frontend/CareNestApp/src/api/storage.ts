import * as Keychain from 'react-native-keychain';

export interface AuthSession {
  token: string;
  refreshToken: string;
  userId: number;
  email: string;
}

const KEYCHAIN_SERVICE = 'com.carenest.session';

let inMemorySession: AuthSession | null = null;

export async function getStoredSession(): Promise<AuthSession | null> {
  if (inMemorySession) {
    return inMemorySession;
  }

  try {
    const credentials = await Keychain.getGenericPassword({ service: KEYCHAIN_SERVICE });
    if (credentials) {
      inMemorySession = JSON.parse(credentials.password) as AuthSession;
      return inMemorySession;
    }
  } catch (error) {
    console.error("Keychain load error", error);
  }
  return null;
}

export async function setStoredSession(session: AuthSession | null): Promise<void> {
  inMemorySession = session;
  
  if (!session) {
    await Keychain.resetGenericPassword({ service: KEYCHAIN_SERVICE });
    return;
  }

  await Keychain.setGenericPassword('carenest_user', JSON.stringify(session), { service: KEYCHAIN_SERVICE });
}
