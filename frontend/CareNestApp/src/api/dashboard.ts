import { apiGetCached } from './client';

export interface DashboardTask {
  type: 'MEDICATION' | 'VACCINATION' | 'APPOINTMENT';
  title: string;
  time: string;
  memberName: string;
  referenceId: number;
  subtitle?: string | null;
}

export interface DashboardPayload {
  unreadNotifications: number;
  todayTasks: DashboardTask[];
  generatedAt?: string;
}

export async function getDashboard(familyId?: number | null, profileId?: number): Promise<DashboardPayload> {
  const params: Record<string, unknown> = {};
  if (familyId) {
    params.familyId = familyId;
  }
  if (profileId) {
    params.profileId = profileId;
  }
  return apiGetCached<DashboardPayload>('/dashboard', params, {
    ttlMs: 20000,
  });
}


