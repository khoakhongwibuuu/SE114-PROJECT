import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

// import removed
import AiChatbotScreen from '../screens/ai/AiChatbotScreen';
import { colors } from '../theme/colors';
import { useFamily } from '../context/FamilyContext';

import FamilyChatStackNavigator from './FamilyChatStackNavigator';

// ─── Top Tab Navigator ───────────────────────────────────────────────────────
export type ChatHubParamList = {
  FamilyChatTab: undefined;
  AiCareTab: undefined;
};

const TopTab = createMaterialTopTabNavigator<ChatHubParamList>();

export default function ChatHubNavigator() {
  return (
    <TopTab.Navigator
      screenOptions={{
        tabBarStyle: styles.tabBar,
        tabBarLabelStyle: styles.tabLabel,
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.outline,
        tabBarIndicatorStyle: styles.indicator,
        tabBarPressColor: '#EFF6FF',
      }}
    >
      <TopTab.Screen
        name="FamilyChatTab"
        component={FamilyChatStackNavigator}
        options={{ tabBarLabel: 'Tổ ấm' }}
      />
      <TopTab.Screen
        name="AiCareTab"
        component={AiChatbotScreen}
        options={{ tabBarLabel: 'AI Care' }}
      />
    </TopTab.Navigator>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    backgroundColor: '#ffffff',
    elevation: 0,
    shadowOpacity: 0,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  tabLabel: {
    fontSize: 14,
    fontWeight: '700',
    letterSpacing: 0.3,
    textTransform: 'none',
  },
  indicator: {
    backgroundColor: colors.primary,
    height: 3,
    borderRadius: 2,
  },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f8fafc',
    padding: 32,
  },
  emptyText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1e293b',
    marginBottom: 8,
    textAlign: 'center',
  },
  emptySubText: {
    fontSize: 14,
    color: '#64748b',
    textAlign: 'center',
    lineHeight: 22,
  },
});
