import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { finalize } from 'rxjs';
import { ResultDialog, ResultDialogData } from './result-dialog';

@Component({
  selector: 'app-root',
  imports: [MatButtonModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  protected readonly busy = signal(false);

  callHello(): void {
    this.call('/hello');
  }

  callError(): void {
    this.call('/error-endpoint');
  }

  private call(url: string): void {
    this.busy.set(true);
    this.http
      .get(url, { responseType: 'text' })
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: (body) => this.open({ kind: 'success', message: body }),
        error: (err: HttpErrorResponse) =>
          this.open({ kind: 'error', traceId: this.traceIdFrom(err.headers) }),
      });
  }

  /** Reads the trace id the server returns in the `X-Trace-Id` response header. */
  private traceIdFrom(headers: HttpHeaders): string | undefined {
    return headers.get('X-Trace-Id') ?? undefined;
  }

  private open(data: ResultDialogData): void {
    this.dialog.open(ResultDialog, { data, width: '420px' });
  }
}
