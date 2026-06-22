import React from 'react';
import { StatusBar } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { NavigationContainer } from '@react-navigation/native';
import { AuthProvider } from './src/context/AuthContext';
import { FamilyProvider } from './src/context/FamilyContext';
import RootNavigator from './src/navigation/RootNavigator';
import OfflineBanner from './src/components/common/OfflineBanner';

function App() {
  return (
    <SafeAreaProvider>
      <NavigationContainer>
        <AuthProvider>
          <FamilyProvider>
            <StatusBar barStyle="dark-content" backgroundColor="#fff" />
            <RootNavigator />
            <OfflineBanner />
          </FamilyProvider>
        </AuthProvider>
      </NavigationContainer>
    </SafeAreaProvider>
  );
}

export default App;
