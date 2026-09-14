import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
} from 'react-native';
import { router } from 'expo-router';
import { colors, spacing } from '@/lib/theme';

export default function DashboardScreen() {
  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.greetingHeader}>
        <Text style={styles.greeting}>Welcome back ??</Text>
        <Text style={styles.subgreeting}>Here is your financial overview for today</Text>
      </View>

      {/* Metric Cards Row */}
      <View style={styles.metricsGrid}>
        <View style={[styles.metricCard, { borderLeftColor: colors.primary }]}>
          <Text style={styles.metricLabel}>Total Revenue</Text>
          <Text style={styles.metricValue}>$24,500.00</Text>
          <Text style={[styles.metricDelta, { color: colors.success }]}>? +12% this month</Text>
        </View>

        <View style={[styles.metricCard, { borderLeftColor: colors.warning }]}>
          <Text style={styles.metricLabel}>Total Expenses</Text>
          <Text style={styles.metricValue}>$8,120.50</Text>
          <Text style={styles.metricDelta}>Recorded in 14 items</Text>
        </View>

        <View style={[styles.metricCard, { borderLeftColor: colors.success }]}>
          <Text style={styles.metricLabel}>Cash Balance</Text>
          <Text style={styles.metricValue}>$16,379.50</Text>
          <Text style={[styles.metricDelta, { color: colors.success }]}>Reconciled across 2 accounts</Text>
        </View>
      </View>

      {/* Quick Actions */}
      <Text style={styles.sectionTitle}>Quick Actions</Text>
      <View style={styles.actionsRow}>
        <TouchableOpacity
          style={styles.actionBtn}
          onPress={() => router.push('/(tabs)/invoices')}
        >
          <Text style={styles.actionIcon}>??</Text>
          <Text style={styles.actionText}>New Invoice</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionBtn}
          onPress={() => router.push('/(tabs)/expenses')}
        >
          <Text style={styles.actionIcon}>??</Text>
          <Text style={styles.actionText}>Record Expense</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.actionBtn}
          onPress={() => router.push('/(tabs)/settings')}
        >
          <Text style={styles.actionIcon}>??</Text>
          <Text style={styles.actionText}>Settings</Text>
        </TouchableOpacity>
      </View>

      {/* Recent Activity */}
      <Text style={styles.sectionTitle}>Recent Activity</Text>
      <View style={styles.activityCard}>
        <View style={styles.activityItem}>
          <View style={[styles.activityIconBadge, { backgroundColor: colors.successLight }]}>
            <Text>??</Text>
          </View>
          <View style={styles.activityTextContainer}>
            <Text style={styles.activityTitle}>Payment Received</Text>
            <Text style={styles.activitySubtitle}>INV-2026-0001 • Acme Corp</Text>
          </View>
          <Text style={[styles.activityAmount, { color: colors.success }]}>+$3,200.00</Text>
        </View>

        <View style={styles.divider} />

        <View style={styles.activityItem}>
          <View style={[styles.activityIconBadge, { backgroundColor: colors.warningLight }]}>
            <Text>??</Text>
          </View>
          <View style={styles.activityTextContainer}>
            <Text style={styles.activityTitle}>Office Rent Paid</Text>
            <Text style={styles.activitySubtitle}>EXP-2026-0004 • Real Estate LLC</Text>
          </View>
          <Text style={[styles.activityAmount, { color: colors.text }]}>-$1,800.00</Text>
        </View>

        <View style={styles.divider} />

        <View style={styles.activityItem}>
          <View style={[styles.activityIconBadge, { backgroundColor: colors.primaryLight }]}>
            <Text>??</Text>
          </View>
          <View style={styles.activityTextContainer}>
            <Text style={styles.activityTitle}>Invoice Issued</Text>
            <Text style={styles.activitySubtitle}>INV-2026-0002 • Globex Ltd</Text>
          </View>
          <Text style={[styles.activityAmount, { color: colors.primary }]}>$5,400.00</Text>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.md,
    paddingBottom: spacing.xl,
  },
  greetingHeader: {
    marginBottom: spacing.lg,
  },
  greeting: {
    fontSize: 24,
    fontWeight: '800',
    color: colors.text,
  },
  subgreeting: {
    fontSize: 14,
    color: colors.textSecondary,
    marginTop: 2,
  },
  metricsGrid: {
    gap: spacing.md,
    marginBottom: spacing.xl,
  },
  metricCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
    borderLeftWidth: 5,
  },
  metricLabel: {
    fontSize: 13,
    fontWeight: '600',
    color: colors.textSecondary,
  },
  metricValue: {
    fontSize: 26,
    fontWeight: '800',
    color: colors.text,
    marginVertical: 4,
  },
  metricDelta: {
    fontSize: 12,
    color: colors.textSecondary,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.md,
  },
  actionsRow: {
    flexDirection: 'row',
    gap: spacing.sm,
    marginBottom: spacing.xl,
  },
  actionBtn: {
    flex: 1,
    backgroundColor: colors.surface,
    padding: spacing.md,
    borderRadius: 14,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: colors.border,
  },
  actionIcon: {
    fontSize: 24,
    marginBottom: 6,
  },
  actionText: {
    fontSize: 12,
    fontWeight: '600',
    color: colors.text,
  },
  activityCard: {
    backgroundColor: colors.surface,
    borderRadius: 16,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
  },
  activityItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: spacing.sm,
  },
  activityIconBadge: {
    width: 40,
    height: 40,
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: spacing.md,
  },
  activityTextContainer: {
    flex: 1,
  },
  activityTitle: {
    fontSize: 15,
    fontWeight: '600',
    color: colors.text,
  },
  activitySubtitle: {
    fontSize: 12,
    color: colors.textSecondary,
    marginTop: 2,
  },
  activityAmount: {
    fontSize: 15,
    fontWeight: '700',
  },
  divider: {
    height: 1,
    backgroundColor: colors.border,
    marginVertical: 4,
  },
});
