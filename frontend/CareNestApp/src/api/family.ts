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
  familyMemberId?: number | null;
  profileId?: number | null;
  userId?: number | null;
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
  ownerUserId?: number;
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
  profileId?: number | null;
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


// --- RAW BACKEND MODELS ---
export interface RawFamilyMember {
  id: number;
  profileId?: number | null;
  user: {
    id: number;
    email: string;
    fullName: string;
    phone?: string;
    dateOfBirth?: string;
    gender?: string;
    avatarUrl?: string;
    role?: string;
    isVerified?: boolean;
  };
  fullName: string;
  avatarUrl?: string;
  role: FamilyRole;
  joinedAt?: string;
}

export interface RawFamilyDetailResponse {
  id: number;
  name: string;
  ownerId?: number;
  createdAt?: string;
  members: RawFamilyMember[];
}

interface RawHealthProfileResponse {
  id: number;
  userId: number;
  familyId: number;
  fullName: string;
  dateOfBirth: string;
  gender: string;
  relationship: string;
  bloodType: string;
  allergies: string;
  chronicDiseases: string;
  notes: string;
  avatarUrl: string;
  isChild: boolean;
  height?: number | null;
  weight?: number | null;
  createdAt: string;
  updatedAt: string;
}

// --- MAPPERS ---
function calculateAge(dateString?: string | null): number | null {
  if (!dateString) return null;
  const birthDate = new Date(dateString);
  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const m = today.getMonth() - birthDate.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }
  return age;
}

function mapRawFamilyToDetail(raw: RawFamilyDetailResponse): FamilyDetailResponse {
  return {
    id: raw.id,
    name: raw.name,
    ownerId: raw.ownerId,
    ownerUserId: raw.ownerId,
    memberCount: raw.members?.length || 0,
    members: (raw.members || []).map(m => ({
      id: m.profileId ?? m.id,
      familyMemberId: m.id,
      profileId: m.profileId ?? null,
      userId: m.user?.id ?? null,
      fullName: m.fullName || m.user?.fullName,
      role: m.role,
      avatarUrl: m.avatarUrl || m.user?.avatarUrl,
      age: calculateAge(m.user?.dateOfBirth),
      healthStatus: 'Bình thường', 
    }))
  };
}

function mapRawHealthProfile(raw: RawHealthProfileResponse): ProfileDetails {
  return {
    id: raw.id,
    profileId: raw.id,
    fullName: raw.fullName,
    birthday: raw.dateOfBirth,
    age: calculateAge(raw.dateOfBirth),
    gender: raw.gender,
    bloodType: raw.bloodType,
    height: raw.height,
    weight: raw.weight,
    medicalHistory: raw.chronicDiseases,
    allergy: raw.allergies,
    emergencyContactPhone: null,
    healthStatus: 'Bình thường',
  };
}

// --- API METHODS ---

export async function getMyFamily(options?: { forceRefresh?: boolean }): Promise<FamilyDetailResponse> {
  const raw = await apiGetCached<RawFamilyDetailResponse>('/families', undefined, { 
    ttlMs: 20000,
    forceRefresh: options?.forceRefresh,
    persist: true,
    offlineMaxAgeMs: 7 * 24 * 60 * 60 * 1000,
  });
  return mapRawFamilyToDetail(raw);
}

export async function createFamily(name: string): Promise<void> {
  await apiPost('/families', { name });
  invalidateApiGetCache(['/families', '/dashboard']);
}

export async function getFamilyProfile(profileId: number): Promise<ProfileDetails> {
  const raw = await apiGetCached<RawHealthProfileResponse>(`/health-profiles/${profileId}`, undefined, {
    ttlMs: 20000,
    persist: true,
    offlineMaxAgeMs: 7 * 24 * 60 * 60 * 1000,
  });
  return mapRawHealthProfile(raw);
}

export async function updateProfile(profileId: number, payload: Record<string, unknown>): Promise<void> {
  await apiPut(`/health-profiles/${profileId}`, {
    fullName: payload.fullName,
    dateOfBirth: payload.birthday,
    gender: payload.gender,
    relationship: payload.relationship || 'UNKNOWN',
    isChild: false, 
    height: payload.height,
    weight: payload.weight,
  });
  
  if (payload.bloodType || payload.allergy || payload.medicalHistory) {
    await apiPut(`/health-profiles/${profileId}/medical-info`, {
      bloodType: payload.bloodType,
      allergies: payload.allergy,
      chronicDiseases: payload.medicalHistory,
    });
  }
  
  invalidateApiGetCache([`/health-profiles/${profileId}`, '/families', '/dashboard']);
}

export async function inviteMember(familyId: number, receiverEmail: string, role: FamilyRole = 'MEMBER'): Promise<void> {
  await apiPost(`/families/${familyId}/invitations`, { email: receiverEmail, role });
  invalidateApiGetCache(['/invitations/sent']);
}

export async function getReceivedInvitations(): Promise<FamilyInvitationItem[]> {
  return apiGetCached<FamilyInvitationItem[]>('/invitations/received', undefined, { ttlMs: 15000 });
}

export async function getSentInvitations(): Promise<FamilyInvitationItem[]> {
  return apiGetCached<FamilyInvitationItem[]>('/invitations/sent', undefined, { ttlMs: 15000 });
}

export async function acceptInvitation(inviteId: number): Promise<void> {
  await apiPut(`/invitations/${inviteId}`, { status: 'ACCEPTED' });
  invalidateApiGetCache(['/families', '/invitations/received', '/dashboard']);
}

export async function rejectInvitation(inviteId: number): Promise<void> {
  await apiPut(`/invitations/${inviteId}`, { status: 'REJECTED' });
  invalidateApiGetCache(['/invitations/received']);
}

export async function getFamilyJoinCode(): Promise<FamilyJoinCodeResponse> {
  return apiGet<FamilyJoinCodeResponse>('/families/join-code');
}

export async function rotateFamilyJoinCode(): Promise<FamilyJoinCodeResponse> {
  return apiPost<FamilyJoinCodeResponse>('/families/join-code/rotate');
}

export async function joinFamilyByCode(joinCode: string, role?: FamilyRole): Promise<FamilyDetailResponse> {
  const response = await apiPost<RawFamilyDetailResponse, { joinCode: string; role?: FamilyRole }>('/families/join-by-code', {
    joinCode,
    role,
  });
  invalidateApiGetCache(['/families', '/dashboard']);
  return mapRawFamilyToDetail(response);
}

export async function joinFamilyByQr(formData: FormData): Promise<FamilyDetailResponse> {
  const response = await apiPost<RawFamilyDetailResponse, FormData>('/families/join-by-qr', formData);
  invalidateApiGetCache(['/families', '/dashboard']);
  return mapRawFamilyToDetail(response);
}

export async function updateFamilyMemberRole(familyId: number, familyMemberId: number, role: FamilyRole): Promise<void> {
  await apiPut(`/families/${familyId}/members/${familyMemberId}/role`, { role });
  invalidateApiGetCache(['/families', '/dashboard']);
}

export async function removeMember(profileId: number): Promise<void> {
  await apiDelete(`/health-profiles/${profileId}`);
  invalidateApiGetCache(['/families', '/dashboard']);
}

// ─── MULTI-FAMILY SUPPORT ────────────────────────────────────────────────────

export interface FamilySummary {
  id: number;
  name: string;
  memberCount: number;
  myRole: FamilyRole;
  ownerName: string;
}

/** Fetch all families the authenticated user belongs to. */
export async function getMyFamilyList(): Promise<FamilySummary[]> {
  return apiGet<FamilySummary[]>('/families/my-list');
}

/** Fetch full detail of a specific family by ID. */
export async function getFamilyById(familyId: number): Promise<FamilyDetailResponse> {
  const raw = await apiGet<RawFamilyDetailResponse>(`/families/${familyId}`);
  return mapRawFamilyToDetail(raw);
}
