import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';
import NetInfo from '@react-native-community/netinfo';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';

export default function OfflineBanner() {
  const insets = useSafeAreaInsets();
  const [isOffline, setIsOffline] = useState(false);

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener(state => {
      setIsOffline(state.isConnected === false || state.isInternetReachable === false);
    });

    void NetInfo.fetch().then(state => {
      setIsOffline(state.isConnected === false || state.isInternetReachable === false);
    });

    return unsubscribe;
  }, []);

  if (!isOffline) {
    return null;
  }

  return (
    <View style={[styles.banner, { paddingTop: insets.top + 8 }]}>
      <MaterialCommunityIcons name="wifi-off" size={18} color="#fff" />
      <Text style={styles.text}>Bạn đang ngoại tuyến. CareNest sẽ hiển thị dữ liệu đã lưu gần nhất khi có thể.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  banner: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 999,
    minHeight: 48,
    paddingHorizontal: 14,
    paddingBottom: 9,
    backgroundColor: '#dc2626',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  text: {
    flexShrink: 1,
    color: '#fff',
    fontSize: 13,
    fontWeight: '800',
    textAlign: 'center',
  },
});
