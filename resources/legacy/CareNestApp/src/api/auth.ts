import { apiGet, apiGetCached, apiPatch, apiPost, apiPut, apiClient, invalidateApiGetCache } from './client';
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
  role?: string | null;
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
    role: raw.role,
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
    refreshToken: data.refreshToken,
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

export async function getCurrentUserProfile(options?: { forceRefresh?: boolean }): Promise<CurrentUserProfile> {
  const [raw, rawProfile] = await Promise.all([
    apiGetCached<RawUserInfoResponse>('/auth/me', undefined, { 
      ttlMs: 20000,
      forceRefresh: options?.forceRefresh 
    }),
    apiGetCached<any>('/health-profiles/me', undefined, {
      ttlMs: 20000,
      forceRefresh: options?.forceRefresh
    }).catch(() => null)
  ]);
  
  const profile = mapRawUserToProfile(raw);
  if (rawProfile) {
    profile.bloodType = rawProfile.bloodType;
    profile.medicalHistory = rawProfile.chronicDiseases;
    profile.allergy = rawProfile.allergies;
    profile.height = rawProfile.height;
    profile.weight = rawProfile.weight;
  }
  return profile;
}



export async function updateCurrentUserProfile(payload: UpdateCurrentUserProfilePayload): Promise<CurrentUserProfile> {
  // 1. Update Core User Account (Full Name, Phone, Birthday)
  const userResponse = await apiPut<RawUserInfoResponse, any>('/auth/me', {
    fullName: payload.fullName,
    phone: payload.phoneNumber,
    dateOfBirth: payload.birthday,
    gender: payload.gender,
  });

  // 2. Update Health Profile (Medical Info)
  // We fetch the correct profileId using the new /health-profiles/me endpoint
  // to avoid ID mismatch (userId != healthProfileId)
  
  try {
    const myProfile = await apiGet<any>('/health-profiles/me');
    const profileId = myProfile.id;

    await apiPut(`/health-profiles/${profileId}`, {
      fullName: payload.fullName,
      dateOfBirth: payload.birthday,
      gender: payload.gender,
      height: payload.height,
      weight: payload.weight,
    });

    if (payload.bloodType || payload.medicalHistory || payload.allergy) {
      await apiPut(`/health-profiles/${profileId}/medical-info`, {
        bloodType: payload.bloodType,
        allergies: payload.allergy,
        chronicDiseases: payload.medicalHistory,
      });
    }

    // Invalidate all related caches
    invalidateApiGetCache(['/auth/me', '/dashboard', '/families', `/health-profiles/${profileId}`]);
  } catch (error) {
    console.error('Failed to update health profile part:', error);
  }
  
  return mapRawUserToProfile(userResponse);
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
