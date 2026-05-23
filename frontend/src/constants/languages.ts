// src/constants/languages.ts
export const languageOptions = [
  { value: 'EN', label: 'English' },
  { value: 'VI', label: 'Vietnamese' },
  { value: 'JA', label: 'Japanese' },
  { value: 'KO', label: 'Korean' },
  { value: 'ZH', label: 'Chinese' },
  { value: 'FR', label: 'French' },
  { value: 'DE', label: 'German' },
  { value: 'ES', label: 'Spanish' },
  { value: 'TH', label: 'Thai' },
  { value: 'RU', label: 'Russian' },
  { value: 'IT', label: 'Italian' },
  { value: 'PT', label: 'Portuguese' },
] as const;

export type LanguageCode = typeof languageOptions[number]['value'];

export const languageMap: Record<LanguageCode, string> = languageOptions.reduce(
  (acc, { value, label }) => ({ ...acc, [value]: label }),
  {} as Record<LanguageCode, string>
);

export function getLanguageLabel(code?: string | null): string {
  if (!code) return 'Không xác định';
  return languageMap[code as LanguageCode] || code;
}