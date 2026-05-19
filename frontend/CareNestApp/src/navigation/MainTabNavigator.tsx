import React, { useEffect } from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { getFocusedRouteNameFromRoute, StackActions } from '@react-navigation/native';
import type { MainTabParamList } from './navigationTypes';

import HomeStack from './HomeStack';
import FamilyStack from './FamilyStack';
import MedicineStack from './MedicineStack';
import ChatHubNavigator from './ChatHubNavigator';
import ProfileStack from './ProfileStack';
import BottomTabBar from '../components/layout/BottomTabBar';
import { useFamily } from '../context/FamilyContext';
import { useAuth } from '../context/AuthContext';
import { getDashboard } from '../api/dashboard';
import { getAppointmentOverview } from '../api/appointments';
import { getCabinetMedicines, getDailySchedule, getScheduleFormData } from '../api/medicine';
import { getNotifications } from '../api/notifications';
import { listConversations } from '../api/ai';
import { formatLocalDate } from '../utils/dateTime';
import { getGrowthSummary } from '../api/growth';
import { getVaccinationTracker } from '../api/vaccinations';
import { getCurrentUserProfile } from '../api/auth';
import { getFamilyProfile } from '../api/family';

const Tab = createBottomTabNavigator<MainTabParamList>();

export default function MainTabNavigator() {
  const { hasFamily, members, selectedProfileId, activeFamilyId } = useFamily();
  const { user } = useAuth();

  const ownProfileId = user?.profileId ? Number(user.profileId) : undefined;
  const activeProfileId = selectedProfileId ?? ownProfileId;
  const notificationProfileId =
    selectedProfileId !== null
      ? selectedProfileId
      : hasFamily
        ? undefined
        : ownProfileId;

  useEffect(() => {
    const today = formatLocalDate(new Date());
    const prefetchTasks: Array<Promise<unknown>> = [
      ...(activeFamilyId ? [getDashboard(activeFamilyId, selectedProfileId ?? undefined)] : []),
      getCabinetMedicines(),
      getScheduleFormData(),
      getNotifications(notificationProfileId),
      listConversations(),
      getCurrentUserProfile(),
    ];

    for (const member of members) {
      prefetchTasks.push(getFamilyProfile(member.id));
    }

    if (typeof activeProfileId === 'number') {
      prefetchTasks.push(getAppointmentOverview(activeProfileId));
      prefetchTasks.push(getDailySchedule(activeProfileId, today));
      prefetchTasks.push(getGrowthSummary(activeProfileId));
      prefetchTasks.push(getVaccinationTracker(activeProfileId));
    }

    prefetchTasks.forEach(task => task.catch(() => {}));
  }, [activeProfileId, members, notificationProfileId, selectedProfileId]);

  const tabPressListener = ({ navigation, route }: any) => ({
    tabPress: (e: any) => {
      const state = navigation.getState();
      const activeRoute = state?.routes?.[state.index];
      const hasHistory = activeRoute?.state && (activeRoute.state.index ?? 0) > 0;

      if (navigation.isFocused() && hasHistory) {
        e.preventDefault();
        
        // Safely navigate to the root screen of the active stack to pop all screens
        if (route.name === 'HomeTab') {
          navigation.navigate('HomeTab', { screen: 'HomeDashboard' });
        } else if (route.name === 'FamilyTab') {
          navigation.navigate('FamilyTab', { screen: 'FamilyPicker' });
        } else if (route.name === 'MedicineTab') {
          navigation.navigate('MedicineTab', { screen: 'MedicineCabinet' });
        } else if (route.name === 'AiChatTab') {
          navigation.navigate('AiChatTab', { screen: 'FamilyChatTab' });
        } else if (route.name === 'ProfileTab') {
          navigation.navigate('ProfileTab', { screen: 'UserProfileSettings' });
        }
      }
    },
  });

  return (
    <Tab.Navigator
      screenOptions={{ 
        headerShown: false,
        animation: 'shift', // Smooth transition between tabs
      }}
      tabBar={props => <BottomTabBar {...props} />}
    >
      <Tab.Screen name="HomeTab" component={HomeStack} listeners={tabPressListener} />
      <Tab.Screen name="FamilyTab" component={FamilyStack} listeners={tabPressListener} />
      <Tab.Screen name="MedicineTab" component={MedicineStack} listeners={tabPressListener} />
      <Tab.Screen name="AiChatTab" component={ChatHubNavigator} listeners={tabPressListener} />
      <Tab.Screen name="ProfileTab" component={ProfileStack} listeners={tabPressListener} />
    </Tab.Navigator>
  );
}
