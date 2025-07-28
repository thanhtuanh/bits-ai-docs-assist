import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';

import { AppComponent } from './app.component';
import { DocumentUploadComponent } from './document-upload/document-upload.component';
import { DocumentSummaryComponent } from './document-summary/document-summary.component';

const routes: Routes = [
    { path: 'upload', component: DocumentUploadComponent },
    { path: 'summary/:id', component: DocumentSummaryComponent },
    { path: '', redirectTo: '/upload', pathMatch: 'full' }
];

@NgModule({
    declarations: [
        AppComponent,
        DocumentUploadComponent,
        DocumentSummaryComponent
    ],
    imports: [
        BrowserModule,
        HttpClientModule,
        RouterModule.forRoot(routes)
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
