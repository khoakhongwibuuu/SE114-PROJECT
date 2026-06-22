export type RootStackParamList = {
  Onboarding: undefined;
  Auth: undefined;
  Main: undefined;
  ChatRoomV2: { familyId: number; familyName: string };
};

export type AuthStackParamList = {
  Login: undefined;
  Register: undefined;
  ForgotPassword: undefined;
  Policy: undefined;
};

export type MainTabParamList = {
  HomeTab: undefined;
  FamilyTab: undefined;
  MedicineTab: undefined;
  AiChatTab: undefined;
  CommunityTab: undefined;
  ProfileTab: undefined;
};

export type HomeStackParamList = {
  HomeDashboard: undefined;
  NotificationsCenter: undefined;
  AppointmentList: { memberId?: string } | undefined;
  AddAppointment: { editId?: string; memberId?: string } | undefined;
  VaccinationTracker: { memberId: string };
  GrowthTracker: { memberId: string };
  MedicineSchedule: { memberId?: string } | undefined;
  AddMedicineSchedule: { editId?: string; memberId?: string } | undefined;
  AddVaccinationSchedule: { profileId: number };
};

export type FamilyStackParamList = {
  FamilyPicker: undefined;
  FamilyManagement: { mode?: 'create' | 'join' } | undefined;
  FamilyChat: { familyId: number; familyName: string };
  HealthProfileDetail: { memberId: string };
  VaccinationTracker: { memberId: string };
  GrowthTracker: { memberId: string };
  UserMedical: { memberId?: string };
  MedicineSchedule: { memberId?: string } | undefined;
  AddMedicineSchedule: { editId?: string; memberId?: string } | undefined;
  AppointmentList: { memberId?: string } | undefined;
  AddAppointment: { editId?: string; memberId?: string } | undefined;
  AddVaccinationSchedule: { profileId: number };
};

export type MedicineStackParamList = {
  MedicineSchedule: { memberId?: string } | undefined;
  MedicineCabinet: undefined;
  AddMedicineSchedule: { editId?: string; memberId?: string } | undefined;
  AddMedicineToCabinet: { editId?: string };
  OcrScanner: undefined;
  AppointmentList: { memberId?: string } | undefined;
  AddAppointment: { editId?: string; memberId?: string } | undefined;
};

export type AiChatStackParamList = {
  AiChatbot: undefined;
};

export type CommunityStackParamList = {
  CommunityTabs: undefined;
  GroupDetail: { groupId: number; groupName: string };
  CreateArticle: undefined;
};

export type CommunityTopTabParamList = {
  WikiTab: undefined;
  GroupsTab: undefined;
};

export type ProfileStackParamList = {
  UserProfileSettings: undefined;
  UserMedical: { memberId?: string };
  DoctorVerification: undefined;
  AdminVerification: undefined;
  Policy: undefined;
  MedicineSchedule: { memberId?: string } | undefined;
  AddMedicineSchedule: { editId?: string; memberId?: string } | undefined;
  AppointmentList: { memberId?: string } | undefined;
  AddAppointment: { editId?: string; memberId?: string } | undefined;
  VaccinationTracker: { memberId: string };
  AddVaccinationSchedule: { profileId: number };
  GrowthTracker: { memberId: string };
};
