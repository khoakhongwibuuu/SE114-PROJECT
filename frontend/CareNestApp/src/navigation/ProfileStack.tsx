import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import type { ProfileStackParamList } from './navigationTypes';

import UserProfileSettingsScreen from '../screens/profile/UserProfileSettingsScreen';
import UserMedicalScreen from '../screens/profile/UserMedicalScreen';
import PolicyScreen from '../screens/profile/PolicyScreen';
import MedicineScheduleScreen from '../screens/medicine/MedicineScheduleScreen';
import AddMedicineScheduleScreen from '../screens/medicine/AddMedicineScheduleScreen';
import AppointmentListScreen from '../screens/appointment/AppointmentListScreen';
import AddAppointmentScreen from '../screens/appointment/AddAppointmentScreen';
import VaccinationTrackerScreen from '../screens/health/VaccinationTrackerScreen';
import AddVaccinationScheduleScreen from '../screens/health/AddVaccinationScheduleScreen';
import GrowthTrackerScreen from '../screens/health/GrowthTrackerScreen';

const Stack = createNativeStackNavigator<ProfileStackParamList>();

export default function ProfileStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="UserProfileSettings" component={UserProfileSettingsScreen} />
      <Stack.Screen name="UserMedical" component={UserMedicalScreen} />
      <Stack.Screen name="Policy" component={PolicyScreen} />
      <Stack.Screen name="MedicineSchedule" component={MedicineScheduleScreen} />
      <Stack.Screen name="AddMedicineSchedule" component={AddMedicineScheduleScreen} />
      <Stack.Screen name="AppointmentList" component={AppointmentListScreen} />
      <Stack.Screen name="AddAppointment" component={AddAppointmentScreen} />
      <Stack.Screen name="VaccinationTracker" component={VaccinationTrackerScreen} />
      <Stack.Screen name="AddVaccinationSchedule" component={AddVaccinationScheduleScreen} />
      <Stack.Screen name="GrowthTracker" component={GrowthTrackerScreen} />
    </Stack.Navigator>
  );
}
