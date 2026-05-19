import React from 'react';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import type { CommunityStackParamList, CommunityTopTabParamList } from './navigationTypes';
import { colors } from '../theme/colors';
import CommunityWikiScreen from '../screens/community/CommunityWikiScreen';
import CommunityGroupsScreen from '../screens/community/CommunityGroupsScreen';
import GroupDetailScreen from '../screens/community/GroupDetailScreen';
import CreateArticleScreen from '../screens/community/CreateArticleScreen';

const Stack = createNativeStackNavigator<CommunityStackParamList>();
const TopTab = createMaterialTopTabNavigator<CommunityTopTabParamList>();

function CommunityTopTabs() {
  return (
    <TopTab.Navigator
      screenOptions={{
        tabBarStyle: {
          backgroundColor: '#fff',
          elevation: 0,
          shadowOpacity: 0,
          borderBottomWidth: 1,
          borderBottomColor: '#e2e8f0',
        },
        tabBarLabelStyle: {
          fontSize: 14,
          fontWeight: '800',
          letterSpacing: 0,
          textTransform: 'none',
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.outline,
        tabBarIndicatorStyle: {
          backgroundColor: colors.primary,
          height: 3,
          borderRadius: 2,
        },
        tabBarPressColor: '#EFF6FF',
      }}
    >
      <TopTab.Screen
        name="WikiTab"
        component={CommunityWikiScreen}
        options={{ tabBarLabel: 'Cam nang' }}
      />
      <TopTab.Screen
        name="GroupsTab"
        component={CommunityGroupsScreen}
        options={{ tabBarLabel: 'Hoi nhom' }}
      />
    </TopTab.Navigator>
  );
}

export default function CommunityStack() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="CommunityTabs" component={CommunityTopTabs} />
      <Stack.Screen name="GroupDetail" component={GroupDetailScreen} />
      <Stack.Screen name="CreateArticle" component={CreateArticleScreen} />
    </Stack.Navigator>
  );
}
