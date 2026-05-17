import { apiDelete, apiGetCached, apiPost, apiPut, invalidateApiGetCache } from './client';
import { getMyFamily } from './family';

export interface MedicineItem {
  id: number;
  name: string;
  quantity: number;
  unit: string;
  expiryDate?: string | null;
  status: string;
}

export interface DailyMedicineSchedule {
  profileName: string;
  date: string;
  sections: Array<{
    session: string;
    label?: string | null;
    items: Array<{
      id: number;
      medicineName: string;
      dosage: string;
      note?: string | null;
      isTaken: boolean;
    }>;
  }>;
}

export interface MedicineScheduleItem {
  id: number;
  profileName: string;
  medicineName: string;
  dosage: string;
  frequency: number;
  sessions?: string[];
  note?: string | null;
  startDate: string;
  endDate: string;
}

export interface MedicineScheduleFormData {
  profiles: Array<{ id: number; fullName: string }>;
  medicines: Array<{ id: number; name: string; quantity: number; unit: string }>;
}

export async function getCabinetMedicines(): Promise<MedicineItem[]> {
  try {
    const family = await getMyFamily();
    const cabinet = await apiGetCached<any>(`/families/${family.id}/cabinets`, undefined, { ttlMs: 20000 });
    return cabinet.medicines.map((m: any) => ({
      id: m.id,
      name: m.medicineName,
      quantity: m.quantity,
      unit: m.unit,
      status: m.quantity <= 0 ? 'OUT_OF_STOCK' : (m.isExpired ? 'EXPIRED' : (m.isExpiring ? 'EXPIRING' : (m.isLowStock ? 'LOW_STOCK' : 'AVAILABLE'))),
      expiryDate: m.expiryDate
    }));
  } catch (error) {
    return [];
  }
}

export async function createCabinetMedicine(payload: {
  name: string;
  quantity: number;
  unit: string;
  expiryDate?: string;
}): Promise<void> {
  const family = await getMyFamily();
  let cabinetId = null;
  try {
    const cabinet = await apiGetCached<any>(`/families/${family.id}/cabinets`);
    cabinetId = cabinet.id;
  } catch (e) {
    const newCabinet = await apiPost<any>('/cabinets', { familyId: family.id, name: 'Tủ thuốc gia đình' });
    cabinetId = newCabinet.id;
  }
  
  await apiPost(`/cabinets/${cabinetId}/medicines`, {
    medicineName: payload.name,
    quantity: payload.quantity,
    unit: payload.unit,
    expiryDate: payload.expiryDate,
    status: 'AVAILABLE'
  });
  invalidateApiGetCache([`/families/${family.id}/cabinets`, '/dashboard']);
}

export async function updateCabinetMedicine(medicineId: number, payload: {
  medicineName?: string;
  quantity?: number;
  unit?: string;
  expiryDate?: string | null;
  notes?: string | null;
}): Promise<void> {
  const family = await getMyFamily();
  const cabinet = await apiGetCached<any>(`/families/${family.id}/cabinets`);
  await apiPut(`/cabinets/${cabinet.id}/medicines/${medicineId}`, payload);
  invalidateApiGetCache([`/families/${family.id}/cabinets`, '/dashboard']);
}

export async function deleteCabinetMedicine(medicineId: number): Promise<void> {
  const family = await getMyFamily();
  const cabinet = await apiGetCached<any>(`/families/${family.id}/cabinets`);
  await apiDelete(`/cabinets/${cabinet.id}/medicines/${medicineId}`);
  invalidateApiGetCache([`/families/${family.id}/cabinets`, '/dashboard']);
}

export async function getDailySchedule(profileId: number, date: string): Promise<DailyMedicineSchedule> {
  const logs = await apiGetCached<any[]>(`/medications/today`, { profileId }, { ttlMs: 15000 });
  
  const morning: any[] = [];
  const noon: any[] = [];
  const evening: any[] = [];

  logs.forEach(log => {
    const time = new Date(log.scheduledTime);
    const hours = time.getHours();
    
    const item = {
      id: log.id,
      medicineName: log.medicineName,
      dosage: log.dosage,
      note: log.notes,
      isTaken: log.status === 'TAKEN'
    };
    
    if (hours < 12) {
      morning.push(item);
    } else if (hours < 17) {
      noon.push(item);
    } else {
      evening.push(item);
    }
  });
  
  const sections = [];
  if (morning.length > 0) sections.push({ session: 'MORNING', items: morning });
  if (noon.length > 0) sections.push({ session: 'NOON', items: noon });
  if (evening.length > 0) sections.push({ session: 'EVENING', items: evening });

  return {
    profileName: 'Hôm nay',
    date: date,
    sections
  };
}

export async function getMedicineSchedules(profileId: number): Promise<MedicineScheduleItem[]> {
  const data = await apiGetCached<any[]>(`/health-profiles/${profileId}/medications`, undefined, { ttlMs: 20000 });
  return data.map(m => ({
    id: m.id,
    profileName: '',
    medicineName: m.medicineName,
    dosage: m.dosage,
    frequency: m.timesPerDay,
    sessions: m.timeSlots,
    note: m.notes,
    startDate: m.startDate,
    endDate: m.endDate
  }));
}

export async function getScheduleFormData(): Promise<MedicineScheduleFormData> {
  const family = await getMyFamily();
  let medicines: any[] = [];
  try {
    const cabinet = await apiGetCached<any>(`/families/${family.id}/cabinets`, undefined, { ttlMs: 30000 });
    medicines = cabinet.medicines || [];
  } catch (error) {
    //
  }

  return {
    profiles: family.members || [],
    medicines: medicines.map((m: any) => ({
      id: m.id,
      name: m.medicineName,
      quantity: m.quantity,
      unit: m.unit,
    })),
  };
}

export async function createMedicineSchedule(payload: {
  profile: number;
  medicineId: number;
  medicineName: string;
  dosage: string;
  frequency: number;
  note?: string;
  startDate: string;
  endDate: string;
}): Promise<void> {
  let timeSlots = ['08:00'];
  if (payload.frequency === 2) timeSlots = ['08:00', '20:00'];
  if (payload.frequency >= 3) timeSlots = ['08:00', '13:00', '20:00'];

  await apiPost(`/health-profiles/${payload.profile}/medications`, {
    medicineName: payload.medicineName,
    dosage: payload.dosage,
    frequency: 'DAILY',
    timesPerDay: payload.frequency,
    timeSlots: timeSlots,
    startDate: payload.startDate,
    endDate: payload.endDate,
    notes: payload.note
  });
  
  invalidateApiGetCache([
    '/medications/today',
    `/health-profiles/${payload.profile}/medications`,
    '/dashboard',
    '/notifications',
  ]);
}

export async function takeDose(payload: { id: number; isTaken: boolean; note?: string }): Promise<void> {
  await apiPost(`/medication-logs/${payload.id}/check-in`, {
    status: payload.isTaken ? 'TAKEN' : 'PENDING',
    notes: payload.note
  });
  invalidateApiGetCache(['/medications/today', '/dashboard', '/notifications']);
}

export async function deleteMedicineSchedule(scheduleId: number): Promise<void> {
  await apiPut(`/medications/${scheduleId}/complete`);
  invalidateApiGetCache(['/medications/today', '/dashboard', '/notifications']);
}

