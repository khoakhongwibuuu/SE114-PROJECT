import React, { useEffect, useMemo, useState } from 'react';
import {
  Alert,
  Dimensions,
  Image,
  KeyboardAvoidingView,
  Modal,
  PermissionsAndroid,
  Platform,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import type { Permission } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { launchCamera, launchImageLibrary, type Asset, type ImagePickerResponse } from 'react-native-image-picker';
import { useNavigation, useFocusEffect, useRoute } from '@react-navigation/native';
import { colors } from '../../theme/colors';
import { BOTTOM_NAV_HEIGHT } from '../../utils/constants';
import { useFamily } from '../../context/FamilyContext';
import { useAuth } from '../../context/AuthContext';
import FAB from '../../components/common/FAB';
import {
  acceptInvitation,
  type FamilyRole,
  getFamilyJoinCode,
  getReceivedInvitations,
  getSentInvitations,
  inviteMember,
  joinFamilyByCode,
  joinFamilyByQr,
  rejectInvitation,
  rotateFamilyJoinCode,
  getFamilyProfile,
  type FamilyInvitationItem,
  type FamilyJoinCodeResponse,
} from '../../api/family';
import { getGrowthSummary } from '../../api/growth';

const { width: SCREEN_WIDTH } = Dimensions.get('window');

const RELATIONS = ['Bß╗æ', 'Mß║╣', 'Anh', 'Chß╗ï', 'Em', 'Kh├íc'] as const;
const JOIN_ROLE_OPTIONS = [
  { label: 'Th├ánh vi├¬n', value: 'MEMBER', icon: 'account' },
  { label: 'Bß╗æ', value: 'FATHER', icon: 'human-male' },
  { label: 'Mß║╣', value: 'MOTHER', icon: 'human-female' },
  { label: 'Anh', value: 'OLDER_BROTHER', icon: 'face-man' },
  { label: 'Chß╗ï', value: 'OLDER_SISTER', icon: 'face-woman' },
  { label: 'Em', value: 'YOUNGER', icon: 'human-child' },
  { label: 'Ng╞░ß╗¥i th├ón', value: 'OTHER', icon: 'account-heart' },
] as const;

type JoinRoleValue = (typeof JOIN_ROLE_OPTIONS)[number]['value'];

function mapRelationToRole(relation: string): FamilyRole {
  switch (relation) {
    case 'Bß╗æ':
      return 'FATHER';
    case 'Mß║╣':
      return 'MOTHER';
    case 'Anh':
      return 'OLDER_BROTHER';
    case 'Chß╗ï':
      return 'OLDER_SISTER';
    case 'Em':
      return 'YOUNGER';
    default:
      return 'OTHER';
  }
}

function formatRole(role?: string | null) {
  switch (role) {
    case 'OWNER':
      return 'Chß╗º gia ─æ├¼nh';
    case 'FATHER':
      return 'Bß╗æ';
    case 'MOTHER':
      return 'Mß║╣';
    case 'OLDER_BROTHER':
      return 'Anh';
    case 'OLDER_SISTER':
      return 'Chß╗ï';
    case 'YOUNGER':
      return 'Em';
    case 'MEMBER':
      return 'Th├ánh vi├¬n';
    case 'OTHER':
      return 'Ng╞░ß╗¥i th├ón';
    default:
      return role || 'Th├ánh vi├¬n';
  }
}

function formatInvitationStatus(status?: string) {
  switch (status) {
    case 'PENDING':
      return '─Éang chß╗¥ x├íc nhß║¡n';
    case 'ACCEPTED':
      return '─É├ú chß║Ñp nhß║¡n';
    case 'REJECTED':
      return '─É├ú tß╗½ chß╗æi';
    default:
      return '─Éang xß╗¡ l├╜';
  }
}

export default function FamilyManagementScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const { hasFamily, family, familyName, createFamily, members, refreshFamily } = useFamily();
  const { user } = useAuth();

  const [overrideMode, setOverrideMode] = useState<'create' | 'join' | null>(null);
  const [step, setStep] = useState(1);

  useEffect(() => {
    const mode = route.params?.mode;
    if (mode) {
      setOverrideMode(mode);
      // Clear navigation parameter so it doesn't get stuck on subsequent visits
      navigation.setParams({ mode: undefined });
    }
  }, [route.params?.mode, navigation]);

  const [tempName, setTempName] = useState('Tß╗ò ß║Ñm th├ón th╞░╞íng');
  const [tempImage, setTempImage] = useState<string | null>(null);
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [selectedRelation, setSelectedRelation] =
    useState<(typeof RELATIONS)[number]>('Mß║╣');
  const [inviteValue, setInviteValue] = useState('');
  const [joinCodeInput, setJoinCodeInput] = useState('');
  const [selectedJoinRole, setSelectedJoinRole] = useState<JoinRoleValue>('MEMBER');
  const [receivedInvitations, setReceivedInvitations] = useState<FamilyInvitationItem[]>([]);
  const [sentInvitations, setSentInvitations] = useState<FamilyInvitationItem[]>([]);
  const [joinCodeInfo, setJoinCodeInfo] = useState<FamilyJoinCodeResponse | null>(null);
  const [isBusy, setIsBusy] = useState(false);

  const myMember = useMemo(
    () => members.find(member => String(member.id) === user?.profileId),
    [members, user?.profileId],
  );
  const isOwner = family?.ownerUserId
    ? family.ownerUserId === user?.userId
    : myMember?.role === 'OWNER';

  const prefetchMemberMedical = (profileId: number) => {
    void getFamilyProfile(profileId);
  };

  const prefetchMemberGrowth = (profileId: number) => {
    void Promise.allSettled([getFamilyProfile(profileId), getGrowthSummary(profileId)]);
  };

  useFocusEffect(
    React.useCallback(() => {
      async function loadFamilyExtras() {
        if (hasFamily) {
          try {
            const sent = isOwner ? await getSentInvitations() : [];
            setSentInvitations(sent);
          } catch {
            setSentInvitations([]);
          }
          setReceivedInvitations([]);
          return;
        }

        try {
          const invites = await getReceivedInvitations();
          setReceivedInvitations(invites);
        } catch (err) {
          console.error('[loadFamilyExtras] getReceivedInvitations failed:', err);
          setReceivedInvitations([]);
        }
        setSentInvitations([]);
        setJoinCodeInfo(null);
      }

      void loadFamilyExtras();
    }, [hasFamily, isOwner])
  );

  const ensureAndroidPermission = async (
    permission: Permission,
    title: string,
    message: string,
  ): Promise<boolean> => {
    if (Platform.OS !== 'android') {
      return true;
    }

    const alreadyGranted = await PermissionsAndroid.check(permission);
    if (alreadyGranted) {
      return true;
    }

    const granted = await PermissionsAndroid.request(permission, {
      title,
      message,
      buttonPositive: 'Cho ph├⌐p',
      buttonNegative: 'Tß╗½ chß╗æi',
      buttonNeutral: '─Éß╗â sau',
    });
    return granted === PermissionsAndroid.RESULTS.GRANTED;
  };

  const ensureCameraPermission = async (): Promise<boolean> => {
    return ensureAndroidPermission(
      PermissionsAndroid.PERMISSIONS.CAMERA,
      'Cho ph├⌐p d├╣ng camera',
      'CareNest cß║ºn quyß╗ün camera ─æß╗â qu├⌐t m├ú QR gia ─æ├¼nh.',
    );
  };

  const ensureLibraryPermission = async (): Promise<boolean> => {
    if (Platform.OS !== 'android') {
      return true;
    }

    const permissions = PermissionsAndroid.PERMISSIONS as Record<string, string | undefined>;
    const permission =
      Platform.Version >= 33
        ? permissions.READ_MEDIA_IMAGES
        : permissions.READ_EXTERNAL_STORAGE;

    if (!permission) {
      return true;
    }

    return ensureAndroidPermission(
      permission as Permission,
      'Cho ph├⌐p truy cß║¡p ß║únh',
      'CareNest cß║ºn quyß╗ün truy cß║¡p ß║únh ─æß╗â qu├⌐t m├ú QR tß╗½ th╞░ viß╗çn.',
    );
  };

  const getAssetFromPickerResponse = (
    response: ImagePickerResponse,
    sourceName: 'camera' | 'th╞░ viß╗çn',
  ): Asset | null => {
    if (response.didCancel) {
      return null;
    }

    if (response.errorCode) {
      Alert.alert(
        `Kh├┤ng thß╗â mß╗ƒ ${sourceName}`,
        response.errorMessage || 'Vui l├▓ng kiß╗âm tra quyß╗ün truy cß║¡p v├á thß╗¡ lß║íi.',
      );
      return null;
    }

    const asset = response.assets?.[0];
    if (!asset) {
      Alert.alert('Kh├┤ng c├│ ß║únh', `Ch╞░a nhß║¡n ─æ╞░ß╗úc ß║únh tß╗½ ${sourceName}.`);
      return null;
    }

    if (!asset.uri) {
      Alert.alert('Kh├┤ng thß╗â ─æß╗ìc ß║únh', 'ß║ónh ch╞░a c├│ dß╗» liß╗çu hß╗úp lß╗ç. Vui l├▓ng thß╗¡ ß║únh kh├íc.');
      return null;
    }

    return asset;
  };

  const handlePickImage = async () => {
    const granted = await ensureLibraryPermission();
    if (!granted) {
      Alert.alert('Thiß║┐u quyß╗ün truy cß║¡p ß║únh', 'Vui l├▓ng cß║Ñp quyß╗ün ─æß╗â chß╗ìn ß║únh tß╗½ th╞░ viß╗çn.');
      return;
    }

    const result = await launchImageLibrary({ mediaType: 'photo', quality: 0.8, selectionLimit: 1 });
    const asset = getAssetFromPickerResponse(result, 'th╞░ viß╗çn');
    if (!asset?.uri) {
      return;
    }

    setTempImage(asset.uri);
  };

  const handleFinishSetup = async () => {
    if (!tempName.trim()) {
      Alert.alert(
        'Thiß║┐u t├¬n gia ─æ├¼nh',
        'Vui l├▓ng nhß║¡p t├¬n tr╞░ß╗¢c khi tß║ío gia ─æ├¼nh.',
      );
      return;
    }

    try {
      setIsBusy(true);
      await createFamily(tempName.trim(), tempImage);
      setStep(1);
      setOverrideMode(null);
    } catch (error) {
      Alert.alert(
        'Kh├┤ng thß╗â tß║ío gia ─æ├¼nh',
        error instanceof Error ? error.message : '─É├ú c├│ lß╗ùi xß║úy ra',
      );
    } finally {
      setIsBusy(false);
    }
  };

  const handleInviteMember = async () => {
    if (!inviteValue.trim()) {
      Alert.alert(
        'Thiß║┐u th├┤ng tin',
        'Vui l├▓ng nhß║¡p email ng╞░ß╗¥i th├ón.',
      );
      return;
    }

    try {
      setIsBusy(true);
      if (!family?.id) {
        throw new Error('Kh├┤ng t├¼m thß║Ñy th├┤ng tin gia ─æ├¼nh.');
      }
      await inviteMember(family.id, inviteValue.trim(), mapRelationToRole(selectedRelation));
      setInviteValue('');
      setSentInvitations(await getSentInvitations());
      Alert.alert(
        '─É├ú gß╗¡i lß╗¥i mß╗¥i',
        'Ng╞░ß╗¥i th├ón cß╗ºa bß║ín sß║╜ nhß║¡n ─æ╞░ß╗úc lß╗¥i mß╗¥i tham gia gia ─æ├¼nh.',
      );
    } catch (error) {
      Alert.alert(
        'Kh├┤ng thß╗â gß╗¡i lß╗¥i mß╗¥i',
        error instanceof Error ? error.message : '─É├ú c├│ lß╗ùi xß║úy ra',
      );
    } finally {
      setIsBusy(false);
    }
  };

  const handleJoinByCode = async () => {
    const code = joinCodeInput.trim();
    if (!code) {
      Alert.alert(
        'Thiß║┐u m├ú tham gia',
        'Vui l├▓ng nhß║¡p m├ú hoß║╖c qu├⌐t m├ú QR ─æß╗â tham gia gia ─æ├¼nh.',
      );
      return;
    }

    try {
      setIsBusy(true);
      await joinFamilyByCode(code, selectedJoinRole);
      await refreshFamily();
      setJoinCodeInput('');
      setSelectedJoinRole('MEMBER');
      setOverrideMode(null);
      Alert.alert(
        'Tham gia th├ánh c├┤ng',
        'Bß║ín ─æ├ú ─æ╞░ß╗úc th├¬m v├áo gia ─æ├¼nh.',
      );
    } catch (error) {
      Alert.alert(
        'Kh├┤ng thß╗â tham gia',
        error instanceof Error ? error.message : '─É├ú c├│ lß╗ùi xß║úy ra',
      );
    } finally {
      setIsBusy(false);
    }
  };

  const handleJoinByQrImage = async (source: 'camera' | 'library') => {
    const granted = source === 'camera'
      ? await ensureCameraPermission()
      : await ensureLibraryPermission();

    if (!granted) {
      Alert.alert(
        source === 'camera' ? 'Thiß║┐u quyß╗ün camera' : 'Thiß║┐u quyß╗ün truy cß║¡p ß║únh',
        source === 'camera'
          ? 'Vui l├▓ng cß║Ñp quyß╗ün camera ─æß╗â qu├⌐t m├ú QR.'
          : 'Vui l├▓ng cß║Ñp quyß╗ün ─æß╗â chß╗ìn ß║únh QR tß╗½ th╞░ viß╗çn.',
      );
      return;
    }

    const picker = source === 'camera' ? launchCamera : launchImageLibrary;
    const result = await picker({
      mediaType: 'photo',
      quality: 0.8,
      selectionLimit: 1,
    });

    const sourceName = source === 'camera' ? 'camera' : 'th╞░ viß╗çn';
    const asset = getAssetFromPickerResponse(result, sourceName);
    if (!asset?.uri) {
      return;
    }

    try {
      setIsBusy(true);
      const formData = new FormData();
      formData.append('image', {
        uri: asset.uri,
        name: asset.fileName || 'family-qr.jpg',
        type: asset.type || 'image/jpeg',
      } as never);
      formData.append('role', selectedJoinRole);
      await joinFamilyByQr(formData);
      await refreshFamily();
      setSelectedJoinRole('MEMBER');
      setOverrideMode(null);
      Alert.alert(
        'Tham gia th├ánh c├┤ng',
        'Bß║ín ─æ├ú qu├⌐t QR v├á tham gia gia ─æ├¼nh.',
      );
    } catch (error) {
      Alert.alert(
        'Kh├┤ng thß╗â qu├⌐t QR',
        error instanceof Error ? error.message : '─É├ú c├│ lß╗ùi xß║úy ra',
      );
    } finally {
      setIsBusy(false);
    }
  };

  const openQrScannerOptions = () => {
    Alert.alert('Qu├⌐t m├ú QR', 'Chß╗ìn c├ích qu├⌐t m├ú gia ─æ├¼nh.', [
      { text: 'Hß╗ºy', style: 'cancel' },
      {
        text: 'Chß╗Ñp bß║▒ng camera',
        onPress: () => {
          setTimeout(() => {
            handleJoinByQrImage('camera');
          }, 150);
        },
      },
      {
        text: 'Chß╗ìn tß╗½ th╞░ viß╗çn',
        onPress: () => {
          setTimeout(() => {
            handleJoinByQrImage('library');
          }, 150);
        },
      },
    ]);
  };

  const handleInvitationAction = async (inviteId: number, action: 'accept' | 'reject') => {
    try {
      setIsBusy(true);
      if (action === 'accept') {
        await acceptInvitation(inviteId);
        const [nextInvitations] = await Promise.all([
          getReceivedInvitations(),
          refreshFamily(),
        ]);
        setReceivedInvitations(nextInvitations);
        setOverrideMode(null);
      } else {
        await rejectInvitation(inviteId);
        setReceivedInvitations(await getReceivedInvitations());
      }
    } catch (error) {
      Alert.alert(
        'Kh├┤ng thß╗â cß║¡p nhß║¡t lß╗¥i mß╗¥i',
        error instanceof Error ? error.message : '─É├ú c├│ lß╗ùi xß║úy ra',
      );
    } finally {
      setIsBusy(false);
    }
  };

  const handleCreateQr = async () => {
    try {
      setIsBusy(true);
      const nextJoinCode = joinCodeInfo
        ? await rotateFamilyJoinCode()
        : await getFamilyJoinCode();
      setJoinCodeInfo(nextJoinCode);
    } catch (error) {
      Alert.alert(
        'Kh├┤ng thß╗â tß║ío m├ú QR',
        error instanceof Error ? error.message : '─É├ú c├│ lß╗ùi xß║úy ra',
      );
    } finally {
      setIsBusy(false);
    }
  };

  const handleOpenAddMemberModal = async () => {
    setAddModalVisible(true);

    if (!isOwner || joinCodeInfo) {
      return;
    }

    try {
      const nextJoinCode = await getFamilyJoinCode();
      setJoinCodeInfo(nextJoinCode);
    } catch {
      setJoinCodeInfo(null);
    }
  };

  const renderOwnerTools = () => {
    if (!isOwner) {
      return null;
    }

    return (
      <View style={styles.inviteCard}>
        <Text style={styles.inputLabel}>TH├èM TH├ÇNH VI├èN Bß║░NG QR</Text>
        <Text style={styles.ownerToolsText}>
          Tß║ío m├ú QR ─æß╗â ng╞░ß╗¥i th├ón qu├⌐t v├á tham gia gia ─æ├¼nh ngay tr├¬n ß╗⌐ng dß╗Ñng.
        </Text>

        <TouchableOpacity
          style={[styles.qrCreateBtn, isBusy && styles.disabledBtn]}
          activeOpacity={0.85}
          onPress={() => void handleCreateQr()}
          disabled={isBusy}
        >
          <MaterialCommunityIcons name="qrcode-plus" size={20} color="#fff" />
          <Text style={styles.qrCreateBtnText}>
            {joinCodeInfo ? 'Tß║ío lß║íi m├ú QR' : 'Tß║ío m├ú QR tham gia'}
          </Text>
        </TouchableOpacity>

        {joinCodeInfo ? (
          <View style={styles.qrPanel}>
            <Image
              source={{ uri: `data:image/png;base64,${joinCodeInfo.qrCodeBase64}` }}
              style={styles.qrPreview}
            />
            <View style={styles.joinCodeBadge}>
              <Text style={styles.joinCodeBadgeText}>{joinCodeInfo.joinCode}</Text>
            </View>
            <Text style={styles.joinCodeHint}>Gia ─æ├¼nh: {joinCodeInfo.name}</Text>

            <Text style={styles.joinCodeHint}>
              Hß║┐t hß║ín: {new Date(joinCodeInfo.expiresAt).toLocaleString('vi-VN')}
            </Text>
          </View>
        ) : null}
      </View>
    );
  };

  const renderInviteContent = () => (
    <ScrollView
      style={styles.root}
      showsVerticalScrollIndicator={false}
      contentContainerStyle={styles.modalContent}
    >
      <View style={styles.centerContainer}>
        <Text style={styles.joinTitle}>Th├¬m th├ánh vi├¬n</Text>
        <Text style={styles.joinSubText}>
          Mß╗¥i ng╞░ß╗¥i th├ón bß║▒ng email hoß║╖c tß║ío m├ú QR ─æß╗â hß╗ì tham gia nhanh v├áo gia ─æ├¼nh cß╗ºa bß║ín.
        </Text>

        <View style={styles.inviteCard}>
          <Text style={styles.inputLabel}>EMAIL NG╞»ß╗£I TH├éN</Text>
          <View style={styles.inviteInputWrap}>
            <MaterialCommunityIcons name="account-box-outline" size={20} color="#64748b" />
            <TextInput
              style={styles.joinInput}
              value={inviteValue}
              onChangeText={setInviteValue}
              placeholder="vidu@email.com"
              placeholderTextColor="#94a3b8"
              keyboardType="email-address"
              autoCapitalize="none"
            />
          </View>

          <Text style={styles.relationLabel}>Mß╗ÉI QUAN Hß╗å</Text>
          <View style={styles.relationGrid}>
            {RELATIONS.map(rel => (
              <TouchableOpacity
                key={rel}
                style={[
                  styles.relationItem,
                  selectedRelation === rel && styles.relationItemSelected,
                ]}
                onPress={() => setSelectedRelation(rel)}
              >
                <MaterialCommunityIcons
                  name={
                    rel === 'Bß╗æ'
                      ? 'human-male'
                      : rel === 'Mß║╣'
                        ? 'human-female'
                        : rel === 'Kh├íc'
                          ? 'dots-horizontal'
                          : 'account'
                  }
                  size={24}
                  color={selectedRelation === rel ? '#0369a1' : '#64748b'}
                />
                <Text
                  style={[
                    styles.relationText,
                    selectedRelation === rel && styles.relationTextSelected,
                  ]}
                >
                  {rel}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <TouchableOpacity
            style={[styles.joinSubmitBtn, isBusy && styles.disabledBtn]}
            activeOpacity={0.8}
            disabled={isBusy}
            onPress={() => void handleInviteMember()}
          >
            <Text style={styles.submitBtnText}>
              {isBusy ? '─Éang gß╗¡i...' : 'Gß╗¡i lß╗¥i mß╗¥i'}
            </Text>
            <MaterialCommunityIcons
              name="send"
              size={20}
              color="#fff"
              style={styles.inlineIcon}
            />
          </TouchableOpacity>
        </View>

        {renderOwnerTools()}

        <View style={styles.pendingSection}>
          <View style={styles.pendingHeader}>
            <MaterialCommunityIcons name="account-clock" size={22} color="#0369a1" />
            <Text style={styles.pendingTitle}>Lß╗¥i mß╗¥i ─æang chß╗¥</Text>
          </View>

          {sentInvitations.length === 0 ? (
            <View style={styles.pendingItem}>
              <View style={styles.pendingAvatar}>
                <Text style={styles.avatarInitial}>0</Text>
              </View>
              <View style={styles.pendingTextWrap}>
                <Text style={styles.pendingEmail}>Ch╞░a c├│ lß╗¥i mß╗¥i n├áo</Text>
                <Text style={styles.pendingStatus}>
                  Danh s├ích sß║╜ hiß╗çn tß║íi ─æ├óy sau khi gß╗¡i.
                </Text>
              </View>
            </View>
          ) : (
            sentInvitations.map(item => (
              <View key={item.inviteId} style={styles.pendingItem}>
                <View style={styles.pendingAvatar}>
                  <Text style={styles.avatarInitial}>
                    {(item.receiverEmail || '?').charAt(0).toUpperCase()}
                  </Text>
                </View>
                <View style={styles.pendingTextWrap}>
                  <Text style={styles.pendingEmail}>
                    {item.receiverEmail || 'Ng╞░ß╗¥i th├ón'}
                  </Text>
                  <Text style={styles.pendingStatus}>{formatInvitationStatus(item.status)}</Text>
                </View>
              </View>
            ))
          )}
        </View>
      </View>
    </ScrollView>
  );

  const renderAddMemberModal = () => (
    <Modal
      visible={addModalVisible}
      transparent={false}
      animationType="slide"
      onRequestClose={() => setAddModalVisible(false)}
    >
      <View style={[styles.root, { paddingTop: insets.top }]}>
        <View style={styles.topBar}>
          <TouchableOpacity style={styles.profileBtn} onPress={() => setAddModalVisible(false)}>
            <MaterialCommunityIcons name="arrow-left" size={24} color="#1e293b" />
          </TouchableOpacity>
          <Text style={styles.topBarTitle}>Th├¬m th├ánh vi├¬n</Text>
          <View style={styles.headerSpacer} />
        </View>

        {renderInviteContent()}
      </View>
    </Modal>
  );

  const renderJoinStep = () => (
    <ScrollView
      style={styles.root}
      contentContainerStyle={styles.modalContent}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.centerContainer}>
        <View style={styles.joinCard}>
          <View style={styles.blueBar} />
          <View style={styles.joinHeroIcon}>
            <MaterialCommunityIcons name="account-group" size={40} color="#1e293b" />
          </View>
          <Text style={styles.joinTitle}>Tham gia gia ─æ├¼nh</Text>
          <Text style={styles.joinSubText}>
            Nhß║¡p m├ú ─æ╞░ß╗úc chia sß║╗ bß╗ƒi ng╞░ß╗¥i th├ón ─æß╗â kß║┐t nß╗æi vß╗¢i tß╗ò ß║Ñm cß╗ºa bß║ín.
          </Text>
          <View style={styles.inputSection}>
            <Text style={styles.inputLabel}>M├â GIA ─É├îNH</Text>
            <View style={styles.joinInputWrap}>
              <MaterialCommunityIcons name="key-variant" size={20} color="#94a3b8" />
              <TextInput
                style={styles.joinInput}
                value={joinCodeInput}
                onChangeText={setJoinCodeInput}
                placeholder="Nhß║¡p m├ú hoß║╖c qu├⌐t m├ú QR"
                placeholderTextColor="#94a3b8"
                autoCapitalize="characters"
              />
            </View>

            <Text style={styles.relationLabel}>VAI TR├Æ Cß╗ªA Bß║áN</Text>
            <View style={styles.joinRoleWrap}>
              {JOIN_ROLE_OPTIONS.map(option => (
                <TouchableOpacity
                  key={option.value}
                  style={[
                    styles.joinRoleChip,
                    selectedJoinRole === option.value && styles.joinRoleChipSelected,
                  ]}
                  onPress={() => setSelectedJoinRole(option.value)}
                >
                  <MaterialCommunityIcons
                    name={option.icon}
                    size={17}
                    color={selectedJoinRole === option.value ? '#0369a1' : '#64748b'}
                  />
                  <Text
                    style={[
                      styles.joinRoleChipText,
                      selectedJoinRole === option.value && styles.joinRoleChipTextSelected,
                    ]}
                  >
                    {option.label}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
          <TouchableOpacity
            style={[styles.joinSubmitBtn, isBusy && styles.disabledBtn]}
            activeOpacity={0.8}
            onPress={() => void handleJoinByCode()}
          >
            <Text style={styles.submitBtnText}>
              {isBusy ? '─Éang xß╗¡ l├╜...' : 'Tham gia bß║▒ng m├ú'}
            </Text>
            <MaterialCommunityIcons name="arrow-right" size={20} color="#fff" />
          </TouchableOpacity>
          <View style={styles.dividerWrap}>
            <View style={styles.dividerLine} />
            <Text style={styles.dividerText}>HOß║╢C</Text>
            <View style={styles.dividerLine} />
          </View>
          <TouchableOpacity style={styles.qrBtn} activeOpacity={0.8} onPress={openQrScannerOptions}>
            <MaterialCommunityIcons name="qrcode-scan" size={20} color="#0369a1" />
            <Text style={styles.qrBtnText}>Qu├⌐t m├ú QR</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.joinBackBtn} onPress={() => {
            if (overrideMode) {
              setOverrideMode(null);
            } else {
              setStep(1);
            }
          }}>
            <MaterialCommunityIcons name="arrow-left" size={18} color="#1e293b" />
            <Text style={styles.joinBackText}>Quay lß║íi</Text>
          </TouchableOpacity>
        </View>

        {receivedInvitations.length > 0 ? (
          <View style={[styles.inviteCard, styles.receivedCard]}>
            <Text style={styles.pendingTitle}>Lß╗¥i mß╗¥i bß║ín ─æ├ú nhß║¡n</Text>
            {receivedInvitations.map(item => (
              <View key={item.inviteId} style={styles.receivedItem}>
                <View style={styles.pendingAvatar}>
                  <Text style={styles.avatarInitial}>
                    {(item.name || '?').charAt(0).toUpperCase()}
                  </Text>
                </View>
                <View style={styles.pendingTextWrap}>
                  <Text style={styles.pendingEmail}>{item.name || 'Gia ─æ├¼nh'}</Text>
                  <Text style={styles.pendingStatus}>
                    {item.senderEmail || 'Kh├┤ng r├╡ ng╞░ß╗¥i gß╗¡i'}
                  </Text>
                </View>
                <View style={styles.actionButtons}>
                  <TouchableOpacity
                    style={[styles.inlineActionBtn, styles.inlineAcceptBtn]}
                    onPress={() => void handleInvitationAction(item.inviteId, 'accept')}
                  >
                    <Text style={styles.inlineAcceptText}>Nhß║¡n</Text>
                  </TouchableOpacity>
                  <TouchableOpacity
                    style={[styles.inlineActionBtn, styles.inlineRejectBtn]}
                    onPress={() => void handleInvitationAction(item.inviteId, 'reject')}
                  >
                    <Text style={styles.inlineRejectText}>Tß╗½ chß╗æi</Text>
                  </TouchableOpacity>
                </View>
              </View>
            ))}
          </View>
        ) : null}
      </View>
    </ScrollView>
  );

  const renderWelcomeStep = () => (
    <ScrollView 
      style={styles.root}
      contentContainerStyle={[styles.centerContainer, { paddingBottom: BOTTOM_NAV_HEIGHT + 32, paddingHorizontal: 16 }]}
      showsVerticalScrollIndicator={false}
    >
      <View style={styles.heroCircle}>
        <MaterialCommunityIcons
          name="account-group-outline"
          size={120}
          color={colors.primary}
        />
      </View>
      <Text style={styles.stepTitle}>Bß║»t ─æß║ºu tß╗ò ß║Ñm cß╗ºa bß║ín</Text>
      <Text style={styles.stepSub}>
        Tß║ío gia ─æ├¼nh ─æß╗â bß║»t ─æß║ºu quß║ún l├╜ sß╗⌐c khß╗Åe cho nhß╗»ng ng╞░ß╗¥i th├ón y├¬u.
      </Text>
      <TouchableOpacity style={styles.primaryBtn} onPress={() => setStep(2)}>
        <MaterialCommunityIcons name="plus" size={24} color="#fff" style={styles.inlineIcon} />
        <Text style={styles.primaryBtnText}>Tß║ío gia ─æ├¼nh</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.secondaryBtn} onPress={() => setStep(4)}>
        <Text style={styles.secondaryBtnText}>Tham gia mß╗Öt gia ─æ├¼nh hiß╗çn c├│</Text>
      </TouchableOpacity>
      <View style={styles.tipCard}>
        <View style={styles.tipIconWrap}>
          <MaterialCommunityIcons name="lightbulb-outline" size={24} color={colors.primary} />
        </View>
        <View style={styles.tipTextWrap}>
          <Text style={styles.tipTitle}>Mß║╣o nhß╗Å cho bß║ín</Text>
          <Text style={styles.tipText}>
            Viß╗çc kß║┐t nß╗æi c├íc th├ánh vi├¬n gi├║p bß║ín theo d├╡i lß╗ïch ti├¬m chß╗ºng v├á nhß║»c nhß╗ƒ uß╗æng thuß╗æc tß╗▒ ─æß╗Öng.
          </Text>
        </View>
      </View>

      {/* Render received invitations directly on Welcome Screen for super UX! */}
      {receivedInvitations.length > 0 ? (
        <View style={[styles.inviteCard, styles.receivedCard, { width: '100%', marginTop: 24 }]}>
          <Text style={styles.pendingTitle}>Lß╗¥i mß╗¥i bß║ín ─æ├ú nhß║¡n</Text>
          {receivedInvitations.map(item => (
            <View key={item.inviteId} style={styles.receivedItem}>
              <View style={styles.pendingAvatar}>
                <Text style={styles.avatarInitial}>
                  {(item.name || '?').charAt(0).toUpperCase()}
                </Text>
              </View>
              <View style={styles.pendingTextWrap}>
                <Text style={styles.pendingEmail}>{item.name || 'Gia ─æ├¼nh'}</Text>
                <Text style={styles.pendingStatus}>
                  {item.senderEmail || 'Kh├┤ng r├╡ ng╞░ß╗¥i gß╗¡i'}
                </Text>
              </View>
              <View style={styles.actionButtons}>
                <TouchableOpacity
                  style={[styles.inlineActionBtn, styles.inlineAcceptBtn]}
                  onPress={() => void handleInvitationAction(item.inviteId, 'accept')}
                >
                  <Text style={styles.inlineAcceptText}>Nhß║¡n</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={[styles.inlineActionBtn, styles.inlineRejectBtn]}
                  onPress={() => void handleInvitationAction(item.inviteId, 'reject')}
                >
                  <Text style={styles.inlineRejectText}>Tß╗½ chß╗æi</Text>
                </TouchableOpacity>
              </View>
            </View>
          ))}
        </View>
      ) : null}
    </ScrollView>
  );

  const renderSetupStep = () => (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={styles.centerContainer}
    >
      <TouchableOpacity style={styles.photoUpload} onPress={handlePickImage} activeOpacity={0.9}>
        {tempImage ? (
          <Image source={{ uri: tempImage }} style={styles.fullPhoto} />
        ) : (
          <MaterialCommunityIcons name="account-group" size={100} color={colors.outline} />
        )}
        <View style={styles.cameraIcon}>
          <MaterialCommunityIcons name="camera" size={20} color="#fff" />
        </View>
      </TouchableOpacity>
      <Text style={styles.stepTitle}>─Éß║╖t t├¬n cho tß╗ò ß║Ñm</Text>
      <Text style={styles.stepSub}>
        T├¬n n├áy sß║╜ hiß╗ân thß╗ï tr├¬n dashboard v├á c├íc b├ío c├ío sß╗⌐c khß╗Åe chung cß╗ºa gia ─æ├¼nh.
      </Text>
      <View style={styles.inputWrap}>
        <MaterialCommunityIcons
          name="account-group-outline"
          size={20}
          color={colors.outline}
          style={styles.inputIcon}
        />
        <TextInput
          style={styles.input}
          placeholder="V├¡ dß╗Ñ: Gia ─æ├¼nh hß║ính ph├║c..."
          value={tempName}
          onChangeText={setTempName}
          placeholderTextColor="#94A3B8"
        />
      </View>
      <View style={styles.tipCardCompact}>
        <View style={styles.tipIconWrap}>
          <MaterialCommunityIcons
            name="information-outline"
            size={24}
            color={colors.secondary}
          />
        </View>
        <View style={styles.tipTextWrap}>
          <Text style={styles.tipTitle}>Mß║╣o nhß╗Å</Text>
          <Text style={styles.tipText}>
            Bß║ín c├│ thß╗â thay ─æß╗òi t├¬n n├áy bß║Ñt cß╗⌐ l├║c n├áo trong phß║ºn c├ái ─æß║╖t gia ─æ├¼nh.
          </Text>
        </View>
      </View>
      <View style={styles.flexSpacer} />
      <TouchableOpacity
        style={[styles.primaryBtn, isBusy && styles.disabledBtn]}
        onPress={() => void handleFinishSetup()}
        disabled={isBusy}
      >
        <Text style={styles.primaryBtnText}>
          {isBusy ? '─Éang tß║ío...' : 'Tiß║┐p tß╗Ñc'}
        </Text>
        <MaterialCommunityIcons name="arrow-right" size={24} color="#fff" style={styles.inlineIcon} />
      </TouchableOpacity>
    </KeyboardAvoidingView>
  );

  const renderManagementStep = () => (
    <View style={styles.managementRoot}>
      <ScrollView
        style={styles.root}
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.managementContent}
      >
        <View style={styles.managementTitleArea}>
          <View style={styles.titleRow}>
            <Text style={styles.managementTitle}>{familyName}</Text>
            <View style={styles.memberPill}>
              <Text style={styles.memberPillText}>{members.length} Th├ánh vi├¬n</Text>
            </View>
          </View>
          <Text style={styles.managementSub}>
            Quß║ún l├╜ sß╗⌐c khß╗Åe v├á lß╗ïch tr├¼nh cß╗ºa cß║ú gia ─æ├¼nh.
          </Text>
        </View>

        <View style={styles.memberList}>
          {members.map(member => {
            const age = member.age ?? 0;
            const isUnder18 = typeof member.age === 'number' && member.age < 18;

            return (
              <View key={member.id} style={styles.memberCard}>
                <View style={styles.memberCardMain}>
                  <View style={styles.avatarWrapper}>
                    <Image
                      source={{
                        uri:
                          member.avatarUrl ||
                          `https://ui-avatars.com/api/?name=${encodeURIComponent(
                            member.fullName,
                          )}&background=eff6ff&color=2563eb&bold=true`,
                      }}
                      style={styles.memberImage}
                    />
                    <View style={styles.statusDot} />
                  </View>

                  <TouchableOpacity
                    style={styles.memberInfo}
                    onPressIn={() => prefetchMemberMedical(member.id)}
                    onPress={() =>
                      navigation.navigate('UserMedical', {
                        memberId: String(member.id),
                      })
                    }
                  >
                    <View style={styles.nameRow}>
                      <Text style={styles.memberName}>{member.fullName}</Text>
                      <View style={styles.roleTag}>
                        <Text style={styles.roleTagText}>{formatRole(member.role)}</Text>
                      </View>
                    </View>
                    <View style={styles.memberMetaRow}>
                      <Text style={styles.memberAge}>{age || '--'} Tuß╗òi</Text>
                    </View>
                  </TouchableOpacity>
                </View>

                {isUnder18 ? (
                  <TouchableOpacity
                    style={styles.growthBar}
                    onPressIn={() => prefetchMemberGrowth(member.id)}
                    onPress={() =>
                      navigation.navigate('GrowthTracker', {
                        memberId: String(member.id),
                      })
                    }
                  >
                    <MaterialCommunityIcons
                      name="human-male-female-child"
                      size={18}
                      color="#0369a1"
                    />
                    <Text style={styles.growthBarText}>THEO D├òI PH├üT TRIß╗éN</Text>
                    <MaterialCommunityIcons
                      name="chevron-right"
                      size={18}
                      color="#0369a1"
                      style={styles.autoMarginLeft}
                    />
                  </TouchableOpacity>
                ) : null}
              </View>
            );
          })}
        </View>
      </ScrollView>

      {isOwner ? (
        <FAB
          onPress={() => void handleOpenAddMemberModal()}
          iconName="add"
          bottomOffset={BOTTOM_NAV_HEIGHT - 55}
        />
      ) : null}
    </View>
  );

  const handleBack = () => {
    if (overrideMode) {
      setOverrideMode(null);
    } else if (navigation.canGoBack()) {
      navigation.goBack();
    }
  };

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <View style={styles.topBar}>
        <View style={styles.topBarLeft}>
          <TouchableOpacity style={styles.profileBtn} onPress={handleBack}>
            {overrideMode || navigation.canGoBack() ? (
              <MaterialCommunityIcons name="arrow-left" size={24} color="#1e293b" />
            ) : (
              <Image
                source={require('../../assets/branding/carenest-logo-house.png')}
                style={styles.smallAvatar}
                resizeMode="contain"
              />
            )}
          </TouchableOpacity>
          <Text style={styles.topBarTitle}>
            {overrideMode === 'create'
              ? 'Tß║ío gia ─æ├¼nh mß╗¢i'
              : overrideMode === 'join'
              ? 'Tham gia gia ─æ├¼nh'
              : hasFamily
              ? familyName
              : 'Gia ─æ├¼nh'}
          </Text>
        </View>
        <View style={styles.headerSpacer} />
      </View>

      {overrideMode
        ? overrideMode === 'create'
          ? renderSetupStep()
          : renderJoinStep()
        : !hasFamily
        ? step === 1
          ? renderWelcomeStep()
          : step === 4
            ? renderJoinStep()
            : renderSetupStep()
        : renderManagementStep()}

      {renderAddMemberModal()}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f8fafc' },
  topBar: {
    height: 60,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    backgroundColor: '#fff',
  },
  topBarTitle: { fontSize: 18, fontWeight: '800', color: '#0369a1' },
  topBarLeft: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  smallAvatar: { width: 32, height: 32 },
  profileBtn: {
    width: 32,
    height: 32,
    alignItems: 'center',
    justifyContent: 'center',
  },
  chatBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#eff6ff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  centerContainer: { flex: 1, padding: 24, alignItems: 'center', justifyContent: 'center' },
  heroCircle: {
    width: 240,
    height: 240,
    borderRadius: 120,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#1a73e8',
    shadowOffset: { width: 0, height: 20 },
    shadowOpacity: 0.1,
    shadowRadius: 30,
    elevation: 10,
    marginBottom: 40,
  },
  stepTitle: {
    fontSize: 28,
    fontWeight: '900',
    color: '#0f172a',
    textAlign: 'center',
    marginBottom: 12,
  },
  stepSub: {
    fontSize: 15,
    color: '#64748b',
    textAlign: 'center',
    lineHeight: 22,
    paddingHorizontal: 20,
    marginBottom: 40,
  },
  primaryBtn: {
    width: '100%',
    height: 56,
    backgroundColor: '#1a73e8',
    borderRadius: 24,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#1a73e8',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.3,
    shadowRadius: 12,
    elevation: 8,
  },
  primaryBtnText: { color: '#fff', fontSize: 17, fontWeight: '700' },
  secondaryBtn: { marginTop: 20, padding: 10 },
  secondaryBtnText: { color: '#1a73e8', fontSize: 15, fontWeight: '600' },
  tipCard: {
    flexDirection: 'row',
    backgroundColor: '#f1f5f9',
    borderRadius: 24,
    padding: 20,
    marginTop: 60,
    alignItems: 'center',
    gap: 16,
  },
  tipCardCompact: {
    flexDirection: 'row',
    backgroundColor: '#f1f5f9',
    borderRadius: 24,
    padding: 20,
    marginTop: 20,
    alignItems: 'center',
    gap: 16,
  },
  tipIconWrap: {
    width: 48,
    height: 48,
    borderRadius: 16,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  tipTitle: { fontSize: 15, fontWeight: '800', color: '#1e293b', marginBottom: 2 },
  tipText: { fontSize: 13, color: '#64748b', lineHeight: 18 },
  tipTextWrap: { flex: 1 },
  photoUpload: {
    width: 200,
    height: 200,
    borderRadius: 100,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    marginBottom: 40,
    position: 'relative',
    overflow: 'hidden',
  },
  fullPhoto: { width: '100%', height: '100%', resizeMode: 'cover' },
  cameraIcon: {
    position: 'absolute',
    bottom: 10,
    right: 10,
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#1a73e8',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 4,
    borderColor: '#fff',
  },
  inputWrap: {
    width: '100%',
    height: 60,
    backgroundColor: '#fff',
    borderRadius: 20,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 20,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  input: { flex: 1, fontSize: 16, color: '#1e293b', fontWeight: '500' },
  inputIcon: { marginRight: 12 },
  managementRoot: { flex: 1 },
  managementContent: { paddingBottom: 120 },
  managementTitleArea: { padding: 24, paddingBottom: 12 },
  titleRow: { flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 8 },
  managementTitle: { fontSize: 32, fontWeight: '900', color: '#1e293b' },
  memberPill: {
    backgroundColor: '#dcf0ff',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 20,
    minWidth: 100,
    alignItems: 'center',
  },
  memberPillText: { fontSize: 13, fontWeight: '700', color: '#0369a1' },
  managementSub: { fontSize: 15, color: '#64748b', lineHeight: 22, width: '80%' },
  memberList: { paddingHorizontal: 20 },
  memberCard: {
    backgroundColor: '#fff',
    borderRadius: 32,
    padding: 16,
    marginBottom: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.05,
    shadowRadius: 12,
    elevation: 3,
  },
  memberCardMain: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  avatarWrapper: { position: 'relative' },
  memberImage: { width: 64, height: 64, borderRadius: 32 },
  statusDot: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 14,
    height: 14,
    borderRadius: 7,
    borderWidth: 2,
    borderColor: '#fff',
    backgroundColor: '#60a5fa',
  },
  memberInfo: { flex: 1, marginLeft: 16 },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 4,
    flexWrap: 'wrap',
  },
  memberName: { fontSize: 18, fontWeight: '800', color: '#1e293b' },
  roleTag: {
    backgroundColor: '#f1f5f9',
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 6,
  },
  roleTagText: { fontSize: 11, fontWeight: '800', color: '#64748b' },
  memberMetaRow: { flexDirection: 'row', alignItems: 'center', marginTop: 4, gap: 10 },
  memberAge: { fontSize: 14, color: '#64748b', fontWeight: '600' },
  growthBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#eff6ff',
    padding: 14,
    borderRadius: 20,
    gap: 10,
  },
  growthBarText: { fontSize: 13, fontWeight: '800', color: '#0369a1', letterSpacing: 0.5 },
  autoMarginLeft: { marginLeft: 'auto' },
  modalContent: { paddingBottom: 40 },
  inviteCard: {
    backgroundColor: '#fff',
    borderRadius: 32,
    padding: 24,
    width: '100%',
    shadowColor: '#1e293b',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.05,
    shadowRadius: 20,
    elevation: 5,
    marginBottom: 24,
  },
  inputLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
    marginBottom: 10,
    marginLeft: 4,
  },
  relationLabel: {
    fontSize: 14,
    fontWeight: '700',
    color: '#475569',
    marginTop: 24,
    marginBottom: 10,
    marginLeft: 4,
  },
  inviteInputWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#e2e8f0',
    borderRadius: 20,
    paddingHorizontal: 16,
    height: 60,
  },
  joinInputWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f1f5f9',
    borderRadius: 20,
    paddingHorizontal: 16,
    height: 60,
  },
  joinInput: {
    flex: 1,
    marginLeft: 12,
    fontSize: 16,
    color: '#0f172a',
    fontWeight: '600',
  },
  relationGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 12, marginTop: 8 },
  relationItem: {
    width: (SCREEN_WIDTH - 48 - 48 - 12) / 2,
    backgroundColor: '#f8fafc',
    borderRadius: 20,
    padding: 16,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: 'transparent',
  },
  relationItemSelected: { borderColor: '#1a73e8', backgroundColor: '#eff6ff' },
  relationText: { fontSize: 14, fontWeight: '700', color: '#64748b', marginTop: 8 },
  relationTextSelected: { color: '#0369a1' },
  joinRoleWrap: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 8 },
  joinRoleChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: '#f8fafc',
    borderRadius: 14,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    paddingHorizontal: 10,
    paddingVertical: 8,
  },
  joinRoleChipSelected: {
    backgroundColor: '#eff6ff',
    borderColor: '#1a73e8',
  },
  joinRoleChipText: { fontSize: 13, fontWeight: '700', color: '#64748b' },
  joinRoleChipTextSelected: { color: '#0369a1' },
  joinSubmitBtn: {
    width: '100%',
    height: 60,
    borderRadius: 20,
    backgroundColor: '#1a73e8',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    shadowColor: '#1a73e8',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
    elevation: 5,
    marginTop: 32,
  },
  submitBtnText: { fontSize: 18, fontWeight: '800', color: '#fff' },
  disabledBtn: { opacity: 0.7 },
  inlineIcon: { marginLeft: 8 },
  ownerToolsText: {
    fontSize: 14,
    color: '#64748b',
    lineHeight: 20,
    marginBottom: 16,
  },
  qrCreateBtn: {
    height: 56,
    borderRadius: 18,
    backgroundColor: '#1a73e8',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    marginBottom: 20,
  },
  qrCreateBtnText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '800',
  },
  qrPanel: {
    alignItems: 'center',
    backgroundColor: '#f8fafc',
    borderRadius: 24,
    padding: 18,
  },
  qrPreview: {
    width: 180,
    height: 180,
    borderRadius: 16,
    marginBottom: 16,
  },
  joinCodeBadge: {
    backgroundColor: '#eff6ff',
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 18,
  },
  joinCodeBadgeText: {
    fontSize: 22,
    fontWeight: '900',
    color: '#0369a1',
    letterSpacing: 2,
  },
  joinCodeHint: {
    fontSize: 13,
    color: '#64748b',
    marginTop: 10,
    textAlign: 'center',
  },
  pendingSection: { width: '100%', marginTop: 24 },
  pendingHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 16,
    paddingHorizontal: 12,
  },
  pendingTitle: { fontSize: 16, fontWeight: '800', color: '#1e293b' },
  pendingItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.02,
    shadowRadius: 8,
    elevation: 1,
    marginBottom: 10,
  },
  receivedCard: { marginTop: 24 },
  receivedItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 20,
    paddingVertical: 12,
    marginTop: 16,
  },
  pendingAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarInitial: { fontSize: 18, fontWeight: '800', color: '#1a73e8' },
  pendingTextWrap: { flex: 1, marginLeft: 12 },
  pendingEmail: { fontSize: 14, fontWeight: '700', color: '#334155' },
  pendingStatus: { fontSize: 12, color: '#94a3b8', marginTop: 2 },
  headerSpacer: { width: 44 },
  joinCard: {
    backgroundColor: '#fff',
    borderRadius: 40,
    padding: 32,
    alignItems: 'center',
    shadowColor: '#1e293b',
    shadowOffset: { width: 0, height: 20 },
    shadowOpacity: 0.08,
    shadowRadius: 40,
    elevation: 10,
    marginTop: 40,
    overflow: 'hidden',
    width: '100%',
  },
  blueBar: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    height: 6,
    backgroundColor: '#1a73e8',
  },
  joinHeroIcon: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 24,
  },
  joinTitle: {
    fontSize: 32,
    fontWeight: '800',
    color: '#0f172a',
    textAlign: 'center',
    marginBottom: 12,
  },
  joinSubText: {
    fontSize: 16,
    color: '#64748b',
    textAlign: 'center',
    lineHeight: 24,
    marginBottom: 32,
  },
  inputSection: { width: '100%', marginBottom: 24 },
  dividerWrap: {
    flexDirection: 'row',
    alignItems: 'center',
    width: '100%',
    marginVertical: 32,
  },
  dividerLine: { flex: 1, height: 1, backgroundColor: '#f1f5f9' },
  dividerText: {
    marginHorizontal: 16,
    fontSize: 14,
    fontWeight: '800',
    color: '#cbd5e1',
    letterSpacing: 1,
  },
  qrBtn: {
    width: '100%',
    height: 60,
    borderRadius: 20,
    backgroundColor: '#f1f5f9',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
  },
  qrBtnText: { fontSize: 16, fontWeight: '800', color: '#0369a1' },
  joinBackBtn: {
    marginTop: 32,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    padding: 10,
  },
  joinBackText: { fontSize: 16, fontWeight: '700', color: '#1a73e8' },
  actionButtons: {
    flexDirection: 'row',
    gap: 8,
  },
  inlineActionBtn: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 12,
  },
  inlineAcceptBtn: {
    backgroundColor: '#dbeafe',
  },
  inlineRejectBtn: {
    backgroundColor: '#fee2e2',
  },
  inlineAcceptText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#1d4ed8',
  },
  inlineRejectText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#dc2626',
  },
  flexSpacer: { flex: 1 },
});
