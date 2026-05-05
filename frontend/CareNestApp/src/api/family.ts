import { apiDelete, apiGet, apiGetCached, apiPost, apiPut, invalidateApiGetCache } from './client';

export type FamilyRole =
  | 'OWNER'
  | 'MEMBER'
  | 'FATHER'
  | 'MOTHER'
  | 'OLDER_BROTHER'
  | 'OLDER_SISTER'
  | 'YOUNGER'
  | 'OTHER';

export interface FamilyMemberSummary {
  id: number;
  fullName: string;
  role: FamilyRole;

  avatarUrl?: string | null;
  age?: number | null;
  healthStatus?: string | null;
}

export interface FamilyDetailResponse {
  id: number;
  name: string;
  ownerId?: number;
  memberCount: number;
  members: FamilyMemberSummary[];
}


export interface FamilyJoinCodeResponse {
  joinCode: string;
  joinLink: string;
  qrCodeBase64?: string;
  expiresAt: string;
  id: number;
  name: string;
}

export interface FamilyInvitationItem {
  inviteId: number;
  id?: number;
  name?: string;
  senderEmail?: string;
  receiverEmail?: string;
  status?: string;
  createdAt?: string;
}


export interface ProfileDetails {
  id: number;
  fullName: string;
  birthday?: string | null;
  age?: number | null;
  gender?: string | null;
  bloodType?: string | null;
  height?: number | null;
  weight?: number | null;
  medicalHistory?: string | null;
  allergy?: string | null;
  emergencyContactPhone?: string | null;
  healthStatus?: string | null;
}


export async function getMyFamily(): Promise<FamilyDetailResponse> {
  return apiGetCached<FamilyDetailResponse>('/families', undefined, { ttlMs: 20000 });
}



export async function createFamily(name: string): Promise<void> {
  await apiPost('/families', { name });
  invalidateApiGetCache(['/families', '/families/profiles/', '/dashboard']);
}



export async function getFamilyProfile(profileId: number): Promise<ProfileDetails> {
  return apiGetCached<ProfileDetails>(`/families/profiles/${profileId}`, undefined, { ttlMs: 20000 });
}



export async function updateProfile(profileId: number, payload: Record<string, unknown>): Promise<void> {
  await apiPut(`/families/update-healthprofile/${profileId}`, payload);
  invalidateApiGetCache([`/families/profiles/${profileId}`, '/families', '/dashboard']);
}

export async function inviteMember(receiverEmail: string, role: FamilyRole): Promise<void> {
  await apiPost('/families/invitations', { receiverEmail, role });
  invalidateApiGetCache(['/families/invitations/']);
}

export async function getReceivedInvitations(): Promise<FamilyInvitationItem[]> {
  return apiGet<FamilyInvitationItem[]>('/families/invitations/received');
}

export async function getSentInvitations(): Promise<FamilyInvitationItem[]> {
  return apiGet<FamilyInvitationItem[]>('/families/invitations/sent');
}

export async function acceptInvitation(inviteId: number): Promise<void> {
  await apiPost(`/families/${inviteId}/accept`);
  invalidateApiGetCache(['/families', '/families/invitations/', '/families/profiles/', '/dashboard']);
}

export async function rejectInvitation(inviteId: number): Promise<void> {
  await apiPost(`/families/${inviteId}/reject`);
  invalidateApiGetCache(['/families/invitations/']);
}

export async function getFamilyJoinCode(): Promise<FamilyJoinCodeResponse> {
  return apiGet<FamilyJoinCodeResponse>('/families/join-code');
}

export async function rotateFamilyJoinCode(): Promise<FamilyJoinCodeResponse> {
  return apiPost<FamilyJoinCodeResponse>('/families/join-code/rotate');
}

export async function joinFamilyByCode(joinCode: string, role?: FamilyRole): Promise<FamilyDetailResponse> {
  const response = await apiPost<FamilyDetailResponse, { joinCode: string; role?: FamilyRole }>('/families/join-by-code', {
    joinCode,
    role,
  });
  invalidateApiGetCache(['/families', '/families/invitations/', '/dashboard']);
  return response;
}

export async function joinFamilyByQr(formData: FormData): Promise<FamilyDetailResponse> {
  const response = await apiPost<FamilyDetailResponse, FormData>('/families/join-by-qr', formData);
  invalidateApiGetCache(['/families', '/families/invitations/', '/dashboard']);
  return response;
}

export async function updateFamilyMemberRole(profileId: number, role: FamilyRole): Promise<FamilyDetailResponse> {
  const response = await apiPut<FamilyDetailResponse, { role: FamilyRole }>(`/families/members/${profileId}/role`, {
    role,
  });
  invalidateApiGetCache(['/families', '/dashboard']);
  return response;
}

export async function removeMember(profileId: number): Promise<void> {
  await apiDelete(`/families/members/${profileId}`);
  invalidateApiGetCache(['/families', '/families/profiles/', '/dashboard']);
}
