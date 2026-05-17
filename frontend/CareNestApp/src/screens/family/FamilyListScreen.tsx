import React from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Image } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { useFamily } from '../../context/FamilyContext';
import type { RootStackParamList } from '../../navigation/navigationTypes';
import { colors } from '../../theme/colors';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'ChatRoomV2'>;

export default function FamilyListScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<NavigationProp>();
  const { family, familyName, familyImage } = useFamily();

  // In the future, this will be an array of families.
  // For now, we mock it with the single family from context if it exists.
  const families = family ? [
    {
      id: family.id,
      name: familyName || 'Tổ ấm thân thương',
      image: familyImage,
      lastMessage: 'Bấm để bắt đầu trò chuyện',
    }
  ] : [];

  if (families.length === 0) {
    return (
      <View style={[styles.emptyState, { paddingTop: insets.top }]}>
        <View style={styles.emptyIconWrap}>
          <MaterialCommunityIcons name="account-group" size={48} color="#94a3b8" />
        </View>
        <Text style={styles.emptyText}>Bạn chưa thuộc gia đình nào.</Text>
        <Text style={styles.emptySubText}>
          Vào tab Gia đình để tạo hoặc tham gia một tổ ấm nhé!
        </Text>
      </View>
    );
  }

  const renderItem = ({ item }: { item: typeof families[0] }) => (
    <TouchableOpacity
      style={styles.familyItem}
      activeOpacity={0.7}
      onPress={() => navigation.navigate('ChatRoomV2', { familyId: item.id, familyName: item.name })}
    >
      <View style={styles.avatarContainer}>
        {item.image ? (
          <Image source={{ uri: item.image }} style={styles.avatar} />
        ) : (
          <View style={[styles.avatar, styles.avatarPlaceholder]}>
            <MaterialCommunityIcons name="home-heart" size={28} color={colors.primary} />
          </View>
        )}
        <View style={styles.onlineDot} />
      </View>
      <View style={styles.infoContainer}>
        <Text style={styles.familyName}>{item.name}</Text>
        <Text style={styles.lastMessage} numberOfLines={1}>{item.lastMessage}</Text>
      </View>
      <MaterialCommunityIcons name="chevron-right" size={20} color="#cbd5e1" />
    </TouchableOpacity>
  );

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <FlatList
        data={families}
        keyExtractor={item => String(item.id)}
        renderItem={renderItem}
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: {
    flex: 1,
    backgroundColor: '#f8fafc',
  },
  listContainer: {
    padding: 16,
  },
  familyItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    padding: 16,
    borderRadius: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 8,
    elevation: 2,
  },
  avatarContainer: {
    position: 'relative',
    marginRight: 16,
  },
  avatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
  },
  avatarPlaceholder: {
    backgroundColor: '#e0f2fe',
    alignItems: 'center',
    justifyContent: 'center',
  },
  onlineDot: {
    position: 'absolute',
    bottom: 2,
    right: 2,
    width: 14,
    height: 14,
    borderRadius: 7,
    backgroundColor: '#22c55e',
    borderWidth: 2,
    borderColor: '#fff',
  },
  infoContainer: {
    flex: 1,
  },
  familyName: {
    fontSize: 16,
    fontWeight: '700',
    color: '#1e293b',
    marginBottom: 4,
  },
  lastMessage: {
    fontSize: 14,
    color: '#64748b',
  },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f8fafc',
    padding: 32,
  },
  emptyIconWrap: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
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
