import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export interface ResultDialogData {
  kind: 'success' | 'error';
  message?: string;
  traceId?: string;
}

@Component({
  selector: 'app-result-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule],
  template: `
    @if (data.kind === 'success') {
      <h2 mat-dialog-title class="title success">Success</h2>
      <mat-dialog-content>
        <p>Received <strong>"{{ data.message }}"</strong> from server.</p>
      </mat-dialog-content>
    } @else {
      <h2 mat-dialog-title class="title error">Error</h2>
      <mat-dialog-content>
        <p>An error has occurred.</p>
        <p class="ref-label">Error Reference</p>
        <code class="ref">{{ data.traceId || 'unavailable' }}</code>
      </mat-dialog-content>
    }
    <mat-dialog-actions align="end">
      <button matButton="filled" mat-dialog-close cdkFocusInitial>OK</button>
    </mat-dialog-actions>
  `,
  styles: `
    .title.success { color: #1b7f3b; }
    .title.error { color: #b3261e; }
    .ref-label { margin: 16px 0 4px; font-weight: 600; }
    .ref {
      display: block;
      padding: 8px 12px;
      border-radius: 6px;
      background: #f4f4f6;
      font-family: ui-monospace, Consolas, monospace;
      user-select: all;
      word-break: break-all;
    }
  `,
})
export class ResultDialog {
  protected readonly data = inject<ResultDialogData>(MAT_DIALOG_DATA);
}
