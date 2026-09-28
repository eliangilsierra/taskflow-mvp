import { fromDateTimeLocalValue, toDateTimeLocalValue } from './date-time';

describe('date-time helpers', () => {
  it('round-trips an instant through the datetime-local format', () => {
    const iso = '2026-03-01T09:30:00.000Z';
    expect(fromDateTimeLocalValue(toDateTimeLocalValue(iso))).toBe(iso);
  });

  it('formats the local time without seconds', () => {
    expect(toDateTimeLocalValue('2026-03-01T09:30:00.000Z')).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
  });

  it('treats empty values as no date', () => {
    expect(toDateTimeLocalValue(null)).toBe('');
    expect(fromDateTimeLocalValue('')).toBeNull();
    expect(fromDateTimeLocalValue('garbage')).toBeNull();
  });
});
