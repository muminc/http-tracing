import { HttpInterceptorFn } from '@angular/common/http';

/** Random lowercase hex string of `bytes` bytes (W3C requires it is not all zeros). */
function randomHex(bytes: number): string {
  const buf = crypto.getRandomValues(new Uint8Array(bytes));
  const hex = Array.from(buf, (b) => b.toString(16).padStart(2, '0')).join('');
  return /^0+$/.test(hex) ? randomHex(bytes) : hex;
}

/**
 * Starts a new W3C trace for every outgoing request by sending
 * `traceparent: 00-<32 hex trace-id>-<16 hex parent-id>-01`.
 */
export const traceparentInterceptor: HttpInterceptorFn = (req, next) => {
  const traceparent = `00-${randomHex(16)}-${randomHex(8)}-01`;
  return next(req.clone({ setHeaders: { traceparent } }));
};
