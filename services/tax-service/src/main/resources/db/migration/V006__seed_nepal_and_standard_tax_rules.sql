-- =============================================================================
-- V006 Seed Nepal (NP) Jurisdiction and Comprehensive CA Rules
-- =============================================================================

INSERT INTO tax_jurisdictions (id, country_code, name, currency_code, is_active)
VALUES ('b3333333-3333-3333-3333-333333333331', 'NP', 'Nepal (Inland Revenue Department)', 'NPR', true)
ON CONFLICT (country_code) DO NOTHING;

-- Tax Categories
INSERT INTO tax_categories (id, jurisdiction_id, code, name, description, tax_type)
VALUES 
  ('c1111111-1111-1111-1111-111111111101', 'b3333333-3333-3333-3333-333333333331', 'VAT_STANDARD', 'Standard Value Added Tax', 'Standard 13% VAT on taxable supply of goods and services', 'VAT'),
  ('c1111111-1111-1111-1111-111111111102', 'b3333333-3333-3333-3333-333333333331', 'VAT_ZERO', 'Zero-Rated VAT', '0% VAT applicable to exports and international transport', 'VAT'),
  ('c1111111-1111-1111-1111-111111111103', 'b3333333-3333-3333-3333-333333333331', 'VAT_EXEMPT', 'Schedule 1 VAT Exempt', 'Exempt goods & services (basic food, medicines, books, education, agriculture)', 'VAT'),
  ('c1111111-1111-1111-1111-111111111104', 'b3333333-3333-3333-3333-333333333331', 'TDS_RENT', 'TDS on House/Building Rent', 'Withholding tax on rental payments to non-corporate landlords', 'WITHHOLDING_TDS'),
  ('c1111111-1111-1111-1111-111111111105', 'b3333333-3333-3333-3333-333333333331', 'TDS_CONTRACT', 'TDS on Contract and Works', 'Withholding tax on works and contract payments exceeding threshold', 'WITHHOLDING_TDS'),
  ('c1111111-1111-1111-1111-111111111106', 'b3333333-3333-3333-3333-333333333331', 'TDS_SERVICE_VAT_REGISTERED', 'TDS on Service (VAT Invoice)', 'Withholding tax on services provided by VAT registered entities', 'WITHHOLDING_TDS'),
  ('c1111111-1111-1111-1111-111111111107', 'b3333333-3333-3333-3333-333333333331', 'TDS_SERVICE_INDIVIDUAL', 'TDS on Consultancy / Service (Non-VAT)', 'Withholding tax on professional consulting and natural persons without VAT', 'WITHHOLDING_TDS'),
  ('c1111111-1111-1111-1111-111111111108', 'b3333333-3333-3333-3333-333333333331', 'TDS_INTEREST', 'TDS on Bank / Debt Interest', 'Withholding tax on interest payments', 'WITHHOLDING_TDS')
ON CONFLICT (jurisdiction_id, code) DO NOTHING;

-- Baseline Statutory Tax Rules (CA Real-world accurate)
INSERT INTO tax_rules (id, jurisdiction_id, category_code, name, rate, threshold_amount, is_reverse_charge, is_exempt, legal_reference, description, effective_from, effective_to, is_active)
VALUES
  (
    'd1111111-1111-1111-1111-111111111101',
    'b3333333-3333-3333-3333-333333333331',
    'VAT_STANDARD',
    'Standard VAT 13%',
    0.1300,
    NULL,
    false,
    false,
    'Value Added Tax Act 2052, Section 7',
    'Standard VAT rate of 13 percent levied on taxable goods and service supplies in Nepal.',
    '2020-01-01',
    NULL,
    true
  ),
  (
    'd1111111-1111-1111-1111-111111111102',
    'b3333333-3333-3333-3333-333333333331',
    'VAT_ZERO',
    'Export Zero-Rated 0%',
    0.0000,
    NULL,
    false,
    false,
    'Value Added Tax Act 2052, Section 7(2)',
    'Export of goods and services outside Nepal charged at zero percent.',
    '2020-01-01',
    NULL,
    true
  ),
  (
    'd1111111-1111-1111-1111-111111111103',
    'b3333333-3333-3333-3333-333333333331',
    'VAT_EXEMPT',
    'Schedule 1 Tax Exempt Goods & Services',
    0.0000,
    NULL,
    false,
    true,
    'Value Added Tax Act 2052, Schedule 1',
    'Exempt from VAT: Agriculture produce, unprocessed foods, health services, educational supplies.',
    '2020-01-01',
    NULL,
    true
  ),
  (
    'd1111111-1111-1111-1111-111111111104',
    'b3333333-3333-3333-3333-333333333331',
    'TDS_RENT',
    'TDS on House Rent 10%',
    0.1000,
    NULL,
    false,
    false,
    'Income Tax Act 2058, Section 88(1)',
    '10% withholding tax deducted on rental of building, land, or premises.',
    '2020-01-01',
    NULL,
    true
  ),
  (
    'd1111111-1111-1111-1111-111111111105',
    'b3333333-3333-3333-3333-333333333331',
    'TDS_CONTRACT',
    'TDS on Contract Works 1.5%',
    0.0150,
    50000.0000,
    false,
    false,
    'Income Tax Act 2058, Section 89(1)',
    '1.5% withholding tax deducted on contract payments exceeding NPR 50,000.',
    '2020-01-01',
    NULL,
    true
  ),
  (
    'd1111111-1111-1111-1111-111111111106',
    'b3333333-3333-3333-3333-333333333331',
    'TDS_SERVICE_VAT_REGISTERED',
    'TDS on Service Fee (VAT Registered Entity) 1.5%',
    0.0150,
    NULL,
    false,
    false,
    'Income Tax Act 2058, Section 88(1) Proviso',
    '1.5% withholding tax on professional service fees when service provider issues a VAT invoice.',
    '2020-01-01',
    NULL,
    true
  ),
  (
    'd1111111-1111-1111-1111-111111111107',
    'b3333333-3333-3333-3333-333333333331',
    'TDS_SERVICE_INDIVIDUAL',
    'TDS on Consultancy / Professional Fees (Non-VAT) 15%',
    0.1500,
    NULL,
    false,
    false,
    'Income Tax Act 2058, Section 88(1)',
    '15% withholding tax on consultation, coaching, and expert service fees from non-VAT registered natural persons.',
    '2020-01-01',
    NULL,
    true
  );

-- Baseline Regulatory Scraping Source
INSERT INTO regulatory_sources (id, jurisdiction_id, name, source_url, scraper_type, is_active)
VALUES (
    'e1111111-1111-1111-1111-111111111101',
    'b3333333-3333-3333-3333-333333333331',
    'Inland Revenue Department (IRD) Nepal Circulars & Directives',
    'https://ird.gov.np/circulars',
    'IRD_CIRCULAR',
    true
);
