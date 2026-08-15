import type { RunContext } from '../types.js';

export function createContext(): RunContext {
  return { lastTappedLocator: null, callStack: new Set(), indentLevel: 0 };
}
