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
  phoneNumber?: string;
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

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: CurrentUserProfile;
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
  return apiGetCached<CurrentUserProfile>('/auth/me', undefined, { ttlMs: 20000 });
}



export async function updateCurrentUserProfile(payload: UpdateCurrentUserProfilePayload): Promise<CurrentUserProfile> {
  const profile = await apiPatch<CurrentUserProfile, UpdateCurrentUserProfilePayload>('/auth/me', payload);
  invalidateApiGetCache(['/auth/me', '/dashboard', '/families/profiles/']);
  return profile;
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
