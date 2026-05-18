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
}

export async function getDashboard(profileId?: number): Promise<DashboardPayload> {
  return apiGetCached<DashboardPayload>('/dashboard', profileId ? { profileId } : undefined, {
    ttlMs: 20000,
  });
}

