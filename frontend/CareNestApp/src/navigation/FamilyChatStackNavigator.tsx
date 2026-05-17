import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import FamilyListScreen from '../screens/family/FamilyListScreen';

export type FamilyChatStackParamList = {
  FamilyList: undefined;
};

const Stack = createNativeStackNavigator<FamilyChatStackParamList>();

export default function FamilyChatStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerShown: false }}>
      <Stack.Screen name="FamilyList" component={FamilyListScreen} />
    </Stack.Navigator>
  );
}
