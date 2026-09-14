import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Modal,
  TextInput,
} from 'react-native';
import { colors, spacing } from '@/lib/theme';

interface ExpenseItem {
  id: string;
  expenseNumber: string;
  vendorName: string;
  category: string;
  totalAmount: string;
  date: string;
}

const initialExpenses: ExpenseItem[] = [
  { id: '1', expenseNumber: 'EXP-2026-0001', vendorName: 'GitHub Copilot', category: 'SOFTWARE', totalAmount: '$20.00', date: 'Oct 01, 2026' },
  { id: '2', expenseNumber: 'EXP-2026-0002', vendorName: 'Amazon Web Services', category: 'UTILITIES', totalAmount: '$240.50', date: 'Oct 03, 2026' },
  { id: '3', expenseNumber: 'EXP-2026-0003', vendorName: 'Downtown Bistro', category: 'MEALS', totalAmount: '$64.00', date: 'Oct 05, 2026' },
  { id: '4', expenseNumber: 'EXP-2026-0004', vendorName: 'Real Estate Holdings', category: 'RENT', totalAmount: '$1,800.00', date: 'Oct 06, 2026' },
];

export default function ExpensesScreen() {
  const [expenses, setExpenses] = useState(initialExpenses);
  const [modalVisible, setModalVisible] = useState(false);
  const [vendor, setVendor] = useState('');
  const [category, setCategory] = useState('OFFICE_SUPPLIES');
  const [amount, setAmount] = useState('');

  const handleAdd = () => {
    if (!vendor || !amount) return;
    const newExp: ExpenseItem = {
      id: Date.now().toString(),
      expenseNumber: `EXP-2026-000${expenses.length + 1}`,
      vendorName: vendor,
      category: category,
      totalAmount: `$${parseFloat(amount).toFixed(2)}`,
      date: 'Today',
    };
    setExpenses([newExp, ...expenses]);
    setVendor('');
    setAmount('');
    setModalVisible(false);
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Expenses</Text>
        <TouchableOpacity
          style={styles.addBtn}
          onPress={() => setModalVisible(true)}
        >
          <Text style={styles.addBtnText}>+ Record</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={expenses}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <View style={styles.cardHeader}>
              <View>
                <Text style={styles.vendor}>{item.vendorName}</Text>
                <Text style={styles.expNumber}>{item.expenseNumber} • {item.date}</Text>
              </View>
              <Text style={styles.amount}>{item.totalAmount}</Text>
            </View>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{item.category}</Text>
            </View>
          </View>
        )}
      />

      {/* Record Expense Modal */}
      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Record Expense</Text>

            <Text style={styles.label}>Vendor / Merchant</Text>
            <TextInput
              style={styles.input}
              placeholder="e.g. AWS, Office Depot"
              placeholderTextColor={colors.textSecondary}
              value={vendor}
              onChangeText={setVendor}
            />

            <Text style={styles.label}>Category</Text>
            <TextInput
              style={styles.input}
              placeholder="e.g. SOFTWARE, UTILITIES, MEALS"
              placeholderTextColor={colors.textSecondary}
              value={category}
              onChangeText={setCategory}
            />

            <Text style={styles.label}>Total Amount ($)</Text>
            <TextInput
              style={styles.input}
              placeholder="0.00"
              placeholderTextColor={colors.textSecondary}
              value={amount}
              onChangeText={setAmount}
              keyboardType="numeric"
            />

            <View style={styles.modalActions}>
              <TouchableOpacity
                style={styles.cancelBtn}
                onPress={() => setModalVisible(false)}
              >
                <Text style={styles.cancelBtnText}>Cancel</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.saveBtn}
                onPress={handleAdd}
              >
                <Text style={styles.saveBtnText}>Save Expense</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: '700',
    color: colors.text,
  },
  addBtn: {
    backgroundColor: colors.primary,
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 10,
  },
  addBtnText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 14,
  },
  list: {
    padding: spacing.md,
    gap: spacing.md,
  },
  card: {
    backgroundColor: colors.surface,
    borderRadius: 14,
    padding: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
    marginBottom: spacing.sm,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: spacing.xs,
  },
  vendor: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
  },
  expNumber: {
    fontSize: 12,
    color: colors.textSecondary,
    marginTop: 2,
  },
  amount: {
    fontSize: 16,
    fontWeight: '800',
    color: colors.text,
  },
  badge: {
    alignSelf: 'flex-start',
    backgroundColor: colors.background,
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
    marginTop: 4,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
    color: colors.textSecondary,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    padding: spacing.lg,
  },
  modalContent: {
    backgroundColor: colors.surface,
    borderRadius: 20,
    padding: spacing.lg,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.text,
    marginBottom: spacing.md,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: colors.text,
    marginTop: spacing.sm,
    marginBottom: 4,
  },
  input: {
    backgroundColor: colors.background,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 10,
    padding: 12,
    fontSize: 15,
    color: colors.text,
  },
  modalActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: spacing.sm,
    marginTop: spacing.lg,
  },
  cancelBtn: {
    paddingHorizontal: 16,
    paddingVertical: 10,
  },
  cancelBtnText: {
    color: colors.textSecondary,
    fontWeight: '600',
  },
  saveBtn: {
    backgroundColor: colors.primary,
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 10,
  },
  saveBtnText: {
    color: '#FFFFFF',
    fontWeight: '700',
  },
});
