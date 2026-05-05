import React, { useEffect, useRef } from 'react';
import { Animated, Image, StyleSheet, View, ActivityIndicator } from 'react-native';
import { colors } from '../../theme/colors';
import { CARENEST_LOGO_HOUSE } from '../../assets/branding';

export default function SplashScreen() {
  const fadeAnim = useRef(new Animated.Value(0)).current;
  const scaleAnim = useRef(new Animated.Value(0.9)).current;

  useEffect(() => {
    Animated.parallel([
      Animated.timing(fadeAnim, {
        toValue: 1,
        duration: 800,
        useNativeDriver: true,
      }),
      Animated.spring(scaleAnim, {
        toValue: 1,
        tension: 10,
        friction: 2,
        useNativeDriver: true,
      }),
    ]).start();
  }, [fadeAnim, scaleAnim]);

  return (
    <View style={styles.container}>
      <Animated.View style={[styles.logoWrap, { opacity: fadeAnim, transform: [{ scale: scaleAnim }] }]}>
        <Image source={CARENEST_LOGO_HOUSE} style={styles.logo} resizeMode="contain" />
      </Animated.View>
      <View style={styles.loaderWrap}>
        <ActivityIndicator size="small" color={colors.primary} />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  logoWrap: {
    width: 140,
    height: 140,
    alignItems: 'center',
    justifyContent: 'center',
  },
  logo: {
    width: '100%',
    height: '100%',
  },
  loaderWrap: {
    position: 'absolute',
    bottom: 80,
  },
});
