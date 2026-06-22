import React from 'react';
import {
    ScrollView,
    StyleSheet,
    Text,
    TouchableOpacity,
    View,
    StatusBar,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';
import Icon from '../../components/common/Icon';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';
import { BOTTOM_NAV_HEIGHT } from '../../utils/constants';

const themedColors = colors;

interface PolicySectionProps {
    icon: string;
    iconBg: string;
    iconColor: string;
    title: string;
    content: string[];
}

function PolicySection({ icon, iconBg, iconColor, title, content }: PolicySectionProps) {
    return (
        <View style={[styles.section, { backgroundColor: themedColors.surface }, shadows.sm]}>
            <View style={styles.sectionHeader}>
                <View style={[styles.iconWrap, { backgroundColor: iconBg }]}>
                    <Icon name={icon} size={22} color={iconColor} />
                </View>
                <Text style={[styles.sectionTitle, { color: themedColors.onSurface }]}>
                    {title}
                </Text>
            </View>
            <View style={styles.sectionContent}>
                {content.map((item, index) => (
                    <View key={index} style={styles.bulletItem}>
                        <View style={[styles.bullet, { backgroundColor: themedColors.primary }]} />
                        <Text style={[styles.bulletText, { color: themedColors.onSurfaceVariant }]}>
                            {item}
                        </Text>
                    </View>
                ))}
            </View>
        </View>
    );
}

export default function PolicyScreen() {
    const insets = useSafeAreaInsets();
    const navigation = useNavigation<any>();

    const sections: PolicySectionProps[] = [
        {
            icon: 'track_changes',
            iconBg: '#E0F2FE',
            iconColor: '#0EA5E9',
            title: '1. Mục đích thu thập thông tin',
            content: ['Quản lý hồ sơ y tế gia đình', 'Cảnh báo và nhắc nhở'],
        },
        {
            icon: 'security',
            iconBg: '#F0FDF4',
            iconColor: '#22C55E',
            title: '2. Các loại dữ liệu thu thập',
            content: [
                'Thông tin hồ sơ sức khỏe',
                'Dữ liệu thuốc',
                'Dữ liệu phát triển trẻ em',
                'Dữ liệu toa thuốc, giọng nói',
            ],
        },
        {
            icon: 'share',
            iconBg: '#FEF2F2',
            iconColor: '#EF4444',
            title: '3. Cách thức sử dụng và chia sẻ dữ liệu',
            content: [
                'Chia sẻ trong nhóm gia đình',
                'Truy xuất khẩn cấp qua QR/NFC',
                'Xử lý bởi dịch vụ AI bên thứ ba',
            ],
        },
        {
            icon: 'lock',
            iconBg: '#FFF7ED',
            iconColor: '#F97316',
            title: '4. Bảo mật dữ liệu',
            content: ['Xác thực mã JWT', 'Lưu trữ trên PostgreSQL'],
        },
        {
            icon: 'policy',
            iconBg: '#F5F3FF',
            iconColor: '#8B5CF6',
            title: '5. Quyền của người dùng',
            content: [
                'Quyền chỉnh sửa hồ sơ',
                'Quản lý vai trò nhóm gia đình',
                'Quyền trích xuất dữ liệu',
            ],
        },
    ];

    return (
        <View style={[styles.root, { backgroundColor: themedColors.background }]}>
            <StatusBar barStyle="light-content" />

            <View style={[styles.header, { paddingTop: insets.top + 10, backgroundColor: '#1E3A8A' }]}>
                <TouchableOpacity
                    style={styles.backBtn}
                    onPress={() => navigation.goBack()}
                >
                    <Icon name="arrow_back" size={26} color="#FFFFFF" />
                </TouchableOpacity>
                <Text style={styles.headerTitle}>Chính sách bảo mật</Text>
                <View style={styles.backBtn} />
            </View>

            <ScrollView
                contentContainerStyle={[
                    styles.scrollContent,
                    { paddingBottom: BOTTOM_NAV_HEIGHT + insets.bottom + 20 },
                ]}
                showsVerticalScrollIndicator={false}
            >
                <View style={styles.introSection}>
                    <Text style={[styles.introText, { color: themedColors.onSurfaceVariant }]}>
                        Tại CareNest, chúng tôi coi trọng sự riêng tư của bạn và cam kết bảo vệ thông tin y tế của gia đình bạn một cách tuyệt đối.
                    </Text>
                </View>

                {sections.map((section, index) => (
                    <PolicySection key={index} {...section} />
                ))}

                <TouchableOpacity
                    style={[styles.downloadBtn, { backgroundColor: '#1E3A8A' }, shadows.md]}
                    onPress={() => { }}
                >
                    <Icon name="description" size={20} color="#FFFFFF" />
                    <Text style={styles.downloadBtnText}>Tải xuống bản đầy đủ</Text>
                </TouchableOpacity>

                <Text style={[styles.footerNote, { color: themedColors.outline }]}>
                    Cập nhật lần cuối: 20/04/2026
                </Text>
            </ScrollView>
        </View>
    );
}

const styles = StyleSheet.create({
    root: {
        flex: 1,
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        paddingHorizontal: 16,
        paddingBottom: 20,
        borderBottomLeftRadius: 24,
        borderBottomRightRadius: 24,
    },
    backBtn: {
        padding: 8,
        width: 42,
    },
    headerTitle: {
        fontSize: 20,
        fontFamily: 'Manrope',
        fontWeight: '800',
        color: '#FFFFFF',
        textAlign: 'center',
    },
    scrollContent: {
        paddingHorizontal: 20,
        paddingTop: 24,
    },
    introSection: {
        marginBottom: 24,
        paddingHorizontal: 4,
    },
    introText: {
        fontSize: 15,
        fontFamily: 'Inter',
        lineHeight: 22,
        textAlign: 'center',
        fontStyle: 'italic',
    },
    section: {
        borderRadius: 20,
        padding: 16,
        marginBottom: 16,
    },
    sectionHeader: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 12,
    },
    iconWrap: {
        width: 40,
        height: 40,
        borderRadius: 12,
        alignItems: 'center',
        justifyContent: 'center',
        marginRight: 12,
    },
    sectionTitle: {
        fontSize: 16,
        fontFamily: 'Manrope',
        fontWeight: '700',
        flex: 1,
    },
    sectionContent: {
        paddingLeft: 52,
    },
    bulletItem: {
        flexDirection: 'row',
        alignItems: 'center',
        marginBottom: 8,
    },
    bullet: {
        width: 6,
        height: 6,
        borderRadius: 3,
        marginRight: 10,
        opacity: 0.6,
    },
    bulletText: {
        fontSize: 14,
        fontFamily: 'Inter',
        fontWeight: '500',
    },
    downloadBtn: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        paddingVertical: 16,
        borderRadius: 16,
        marginTop: 12,
        marginBottom: 16,
        gap: 8,
    },
    downloadBtnText: {
        fontSize: 16,
        fontFamily: 'Inter',
        fontWeight: '700',
        color: '#FFFFFF',
    },
    footerNote: {
        fontSize: 12,
        fontFamily: 'Inter',
        textAlign: 'center',
        marginTop: 8,
    },
});