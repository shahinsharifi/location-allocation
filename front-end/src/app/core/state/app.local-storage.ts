export function saveState(state: any) {
  try {
    const serializedState = JSON.stringify(state);
    localStorage.setItem('app_state', serializedState);
  } catch (error) {
    console.error('Error saving state:', error);
  }
}

export function loadState() {
  try {
    const serializedState = localStorage.getItem('app_state');
    if (serializedState === null) {
      return undefined;
    }
    return JSON.parse(serializedState);
  } catch (error) {
    console.error('Error loading state:', error);
    return undefined;
  }
}
