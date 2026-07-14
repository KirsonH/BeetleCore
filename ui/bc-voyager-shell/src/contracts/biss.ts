export interface BissMessage {
  header: BissHeader;
  context: BissContext;
  audit: BissAudit;
  body: Record<string, unknown>;
}

export interface BissHeader {
  token: string;
  transactionCode: string;
  system: string;
  isSandbox: boolean;
  openBankingConsentId?: string;
  authorizationToken?: string;
}

export interface BissContext {
  tenantId: string;
  branchId: string;
  bookId: string;
  systemLocale: string;
  systemTimezone: string;
  systemCurrency: string;
  geolocation: BissGeolocation;
  dataJurisdiction: string;
}

export interface BissGeolocation {
  latitude: number;
  longitude: number;
  country: string;
  region: string;
}

export interface BissAudit {
  operatorId: string;
  clientSessionId: string;
}
