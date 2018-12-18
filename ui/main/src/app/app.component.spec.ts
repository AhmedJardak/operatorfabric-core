/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import {async, TestBed} from '@angular/core/testing';
import {AppComponent} from './app.component';
import {RouterTestingModule} from '@angular/router/testing';
import {MatTabsModule, MatToolbarModule} from '@angular/material';
import {Store, StoreModule} from '@ngrx/store';
import {appReducer, AppState} from '@ofStore/index';
import {BrowserDynamicTestingModule, platformBrowserDynamicTesting} from '@angular/platform-browser-dynamic/testing';
import {of} from 'rxjs';
import {NavbarComponent} from './components/navbar/navbar.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {selectCurrentUrl} from '@ofSelectors/router.selectors';



describe('AppComponent', () => {

    let store: Store<AppState>;

    let fixture;

    let component;

    beforeEach(async(() => {
        TestBed.resetTestEnvironment();
        TestBed.initTestEnvironment(BrowserDynamicTestingModule,
            platformBrowserDynamicTesting());
        TestBed.configureTestingModule({
            imports: [
                NgbModule.forRoot(),
                StoreModule.forRoot(appReducer),
                // solution 4 RouterTestingModule: https://github.com/coreui/coreui-free-bootstrap-admin-template/issues/202
                RouterTestingModule,
                MatTabsModule,
                MatToolbarModule
            ],
            declarations: [AppComponent,NavbarComponent],
            providers: [{provide: store, useClass: Store}
            ]
        }).compileComponents();
        store = TestBed.get(Store);
        spyOn(store, 'dispatch').and.callThrough();
        // avoid exceptions during construction and init of the component
        spyOn(store, 'select').and.callFake((obj) => {
            if (obj === selectCurrentUrl) {
                // called in ngOnInit and passed to mat-tab-link
                return of('/test/url');
            }
            console.log('passed');
            return of({});
        }
    );
        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
    }));

    it('should create the app', async(() => {
        const app = fixture.debugElement.componentInstance;
        expect(app).toBeTruthy();
    }));

    it(`should have as title 'Operator Fabric'`, async(() => {
      const app = fixture.debugElement.componentInstance;
      expect(app.title).toEqual('Operator Fabric');
    }));

    // it('should render title in the toolbar', async(() => {
    //     expect(component).toBeDefined();
    //     const toolBarText = fixture.debugElement.query(By.directive(MatToolbar)).nativeElement;
    //     fixture.detectChanges();
    //     expect(toolBarText).toBeDefined();
    //     expect(toolBarText).toBeTruthy();
    //     expect(toolBarText.textContent).toContain('Operator Fabric');
    // }));
});
