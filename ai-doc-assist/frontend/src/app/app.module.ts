import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AppComponent } from './app.component';
import { DocumentUploadComponent } from './document-upload/document-upload.component';
import { DocumentSummaryComponent } from './document-summary/document-summary.component';
import { FeedbackComponent } from './feedback/feedback.component';
import { FeedbackService } from './feedback.service';

const routes: Routes = [
  { path: '', component: DocumentUploadComponent, pathMatch: 'full' },
  { path: 'summary/:id', component: DocumentSummaryComponent },
  { path: '**', redirectTo: '', pathMatch: 'full' }
];

@NgModule({
  declarations: [
    AppComponent,
    DocumentUploadComponent,
    DocumentSummaryComponent,
    FeedbackComponent 
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    RouterModule.forRoot(routes)
  ],
  providers: [FeedbackService],
  bootstrap: [AppComponent]
})
export class AppModule {}
