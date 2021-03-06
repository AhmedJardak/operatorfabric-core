/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Action, Store} from '@ngrx/store';
import {Observable, of, zip} from 'rxjs';
import {catchError, map, switchMap} from 'rxjs/operators';
import {AppState} from "@ofStore/index";
import {LoadCard} from "@ofActions/card.actions";
import {ThirdsService} from "@ofServices/thirds.service";
import {LoadMenuFailure, LoadMenuSuccess, MenuActionTypes} from "@ofActions/menu.actions";

@Injectable()
export class MenuEffects {

    /* istanbul ignore next */
    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private service: ThirdsService
    ) {
    }

    @Effect()
    load: Observable<Action> = this.actions$
        .pipe(
            ofType<LoadCard>(MenuActionTypes.LoadMenu),
            switchMap(action => this.service.computeThirdsMenu()),
            switchMap(menu=>zip(of(menu),this.service.loadI18nForMenuEntries(menu)
                .pipe(
                    catchError((err,caught)=>{
                        console.error(err);
                        return of(false);
                    })
                ))),
            map(array =>
                new LoadMenuSuccess({menu: array[0]})
            ),
            catchError((err, caught) => {
                console.error(err);
                this.store.dispatch(new LoadMenuFailure(err));
                return caught;
            })
        );
}
