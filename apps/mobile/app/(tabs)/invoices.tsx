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

interface InvoiceItem {
  id: string;
  invoiceNumber: string;
  customerName: string;
  totalAmount: string;
  dueDate: string;
  status: 'PAID' | 'ISSUED' | 'DRAFT';
}

const initialInvoices: InvoiceItem[] = [
  { id: '1', invoiceNumber: 'INV-2026-0001', customerName: 'Acme Technologies', totalAmount: '$3,200.00', dueDate: 'Oct 15, 2026', status: 'PAID' },
  { id: '2', invoiceNumber: 'INV-2026-0002', customerName: 'Globex Corporation', totalAmount: '$5,400.00', dueDate: 'Oct 28, 2026', status: 'ISSUED' },
  { id: '3', invoiceNumber: 'INV-2026-0003', customerName: 'Soylent Financial', totalAmount: '$1,850.00', dueDate: 'Nov 02, 2026', status: 'DRAFT' },
];

export default function InvoicesScreen() {
  const [invoices, setInvoices] = useState(initialInvoices);
  const [modalVisible, setModalVisible] = useState(false);
  const [customer, setCustomer] = useState('');
  const [amount, setAmount] = useState('');

  const handleCreate = () => {
    if (!customer || !amount) return;
    const newInv: InvoiceItem = {
      id: Date.now().toString(),
      invoiceNumber: `INV-2026-000${invoices.length + 1}`,
      customerName: customer,
      totalAmount: `$${parseFloat(amount).toFixed(2)}`,
      dueDate: 'In 30 days',
      status: 'ISSUED',
    };
    setInvoices([newInv, ...invoices]);
    setCustomer('');
    setAmount('');
    setModalVisible(false);
  };

  const getBadgeStyle = (status: InvoiceItem['status']) => {
    switch (status) {
      case 'PAID':
        return { bg: colors.successLight, text: colors.success };
      case 'ISSUED':
        return { bg: colors.primaryLight, text: colors.primary };
      default:
        return { bg: colors.border, text: colors.textSecondary };
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Invoices</Text>
        <TouchableOpacity
          style={styles.addBtn}
          onPress={() => setModalVisible(true)}
        >
          <Text style={styles.addBtnText}>+ New</Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={invoices}
        keyExtractor={(item) => item.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => {
          const badge = getBadgeStyle(item.status);
          return (
            <View style={styles.card}>
              <View style={styles.cardHeader}>
                <View>
                  <Text style={styles.invNumber}>{item.invoiceNumber}</Text>
                  <Text style={styles.customerName}>{item.customerName}</Text>
                </View>
                <View style={[styles.badge, { backgroundColor: badge.bg }]}>
                  <Text style={[styles.badgeText, { color: badge.text }]}>{item.status}</Text>
                </View>
              </View>
              <View style={styles.cardFooter}>
                <Text style={styles.dueText}>Due: {item.dueDate}</Text>
                <Text style={styles.amount}>{item.totalAmount}</Text>
              </View>
            </View>
          );
        }}
      />

      {/* New Invoice Modal */}
      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>Create New Invoice</Text>

            <Text style={styles.label}>Customer / Client Name</Text>
            <TextInput
              style={styles.input}
              placeholder="e.g. Acme Corp"
              placeholderTextColor={colors.textSecondary}
              value={customer}
              onChangeText={setCustomer}
            />

            <Text style={styles.label}>Amount ($)</Text>
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
                onPress={handleCreate}
              >
                <Text style={styles.saveBtnText}>Issue Invoice</Text>
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
    marginBottom: spacing.sm,
  },
  invNumber: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
  },
  customerName: {
    fontSize: 13,
    color: colors.textSecondary,
    marginTop: 2,
  },
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '700',
  },
  cardFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: spacing.xs,
    borderTopWidth: 1,
    borderTopColor: colors.border,
  },
  dueText: {
    fontSize: 12,
    color: colors.textSecondary,
  },
  amount: {
    fontSize: 16,
    fontWeight: '800',
    color: colors.text,
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
