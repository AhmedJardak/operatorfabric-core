/* Copyright (c) 2018, RTE (http://www.rte-france.com)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Observable, of} from 'rxjs';
import {Store} from '@ngrx/store';
import {
    AcceptLogIn,
    AcceptLogOut,
    AcceptLogOutSuccess,
    AuthenticationActions,
    AuthenticationActionTypes,
    RejectLogIn,
    TryToLogIn,
    TryToLogOut
} from '@ofActions/authentication.actions';
import {AuthenticationService, CheckTokenResponse} from '../../services/authentication.service';
import {catchError, map, switchMap, tap} from 'rxjs/operators';
import {AppState} from "@ofStore/index";
import {Router} from "@angular/router";

/**
 * Management of the authentication of the current user
 */
@Injectable()
export class AuthenticationEffects {

    /**
     * @constructor
     * @param store - {Store<AppState>} state manager
     * @param action$ - {Action} {Observable} of Action of the Application
     * @param authService - service implementing the authentication business rules
     * @param router - router service to redirect user accordingly to the user authentication status or variation of it.
     *
     * istanbul ignore next */
    constructor(private store: Store<AppState>,
                private actions$: Actions,
                private authService: AuthenticationService,
                private router: Router) {
    }

    /**
     * This {Observable} of {AuthenticationActions} listen {AuthenticationActionTypes.TryToLogIn} type and uses
     * the login payload to get an authentication token from the authentication service if the authentication is
     * valid and emit an {AcceptLogin} action with it.
     * For invalid authentication it emits a {RejectLogIn} Action with the message "unable to authenticate the user"
     * as paload
     *
     * This effect should take action after the user has submitted login information in the login page by clicking
     * on the login button.
     *
     * @member
     * @name TryToLogin
     * @typedef {Observable<AuthenticationActions>}
     */
    @Effect()
    TryToLogIn: Observable<AuthenticationActions> =
        this.actions$
            .pipe(
                ofType(AuthenticationActionTypes.TryToLogIn),
                switchMap((action: TryToLogIn) => {
                    const payload = action.payload;
                    return this.authService.askToken(payload.username, payload.password).pipe(
                        map(authenticationInfo => new AcceptLogIn(authenticationInfo)),
                        catchError(error => {
                                console.error('error while trying log in', error);
                                return of(new RejectLogIn({denialReason: 'unable to authenticate the user'}));
                            }
                        ));
                })
            );

    /**
     * This {Observable} of {AuthenticationActions} listens for {AuthenticationActionTypes.TryToLogOut} type.
     * It tells the {AuthenticationService} to clear the authentication information from the system and emit
     * an {AcceptLogOut} Action.
     *
     * This effect should take action after the Logout link/button has been clicked by the user.
     *
     * @member
     * @name TryToLogOut
     * @typedef {Observable<AuthenticationActions>}
     */
    @Effect()
    TryToLogOut: Observable<AuthenticationActions> =
        this.actions$.pipe(
            ofType(AuthenticationActionTypes.TryToLogOut),
            map((action: TryToLogOut) => {
                this.authService.clearAuthenticationInformation();
                return new AcceptLogOut();
            })
        );

    /**
     * This {Observable} of {AuthenticationActions} listens for {AuthenticationActionTypes.AcceptLogOut} type.
     * It tells the {Router} service to navigate to the Login page. and emit an {AcceptLogOutSucess} Action.
     *
     * This {Effect} should be called as a consequence of a {TryLogOut} action
     *
     * @member
     * @name AcceptLogOut
     * @typedef {Observable<AuthenticationActions>}
     *
     */
    @Effect()
    AcceptLogOut: Observable<AuthenticationActions> =
        this.actions$.pipe(
            ofType(AuthenticationActionTypes.AcceptLogOut),
            map((action: AcceptLogOut) => {
                this.router.navigate(['/login'])
                return new  AcceptLogOutSuccess();
            })
        );
    /**
     * This {Observable} of {AuthenticationActions} listens for {AuthenticationActionTypes.RejectLogIn} type.
     * It tells the {AuthenticationService} to clear authentication information from the system
     *
     * This {Effect} should be called after a wrong login attempt.

     * @member
     * @name RejectLogInAttempt
     * @typedef {Observable<AuthenticationActions>}
     */
    @Effect()
    RejectLogInAttempt: Observable<AuthenticationActions> =
        this.actions$.pipe(ofType(AuthenticationActionTypes.RejectLogIn),
            tap(() => {
                this.authService.clearAuthenticationInformation();
            }),
            map(action => new AcceptLogOut()));

    /**
     * This {Observable} of {AuthenticationActions} listens for {AuthenticationActionTypes.CheckAuthenticationStatus} type.
     *It extract the current authentication information if any and checks its validity, the expiration date.
     *  If it's OK then an {AcceptLogIn} Action with check result as payload is emittedby calling
     *  the {handleLogInAttempt} @method,
     *
     *  otherwise a {RejectedLogIn} Action is emitted by calling the {handleRejectedLogin} @method.
     *
     * This {Effect} should be the first effect apply by the application to display the right page accordingly with the
     * current authentication state.
     *
     * @member
     * @name CheckAuthentication
     * @typedef {Observable<AuthenticationActions>}
     *
     */
    @Effect()
    CheckAuthentication: Observable<AuthenticationActions> =
        this.actions$
            .pipe(
                ofType(AuthenticationActionTypes.CheckAuthenticationStatus),
                switchMap(() => {
                    return this.authService.checkAuthentication(this.authService.extractToken());
                }),
                map((payload: CheckTokenResponse) => {
                    if (this.authService.isExpirationDateOver()) {
                        return this.handleRejectedLogin('expiration date exceeded');
                    }

                    return this.handleLogInAttempt(payload);
                }),
                catchError((err, caught) => {
                    console.error(err);
                    this.store.dispatch(new RejectLogIn({denialReason: err}));
                    return caught;
                })
            );

    handleRejectedLogin(errorMsg: string): AuthenticationActions {
        this.authService.clearAuthenticationInformation();
        return new RejectLogIn({denialReason: errorMsg});

    }

    handleLogInAttempt(payload: CheckTokenResponse): AuthenticationActions {
        if (payload) {
            const authInfo = this.authService.extractIdentificationInformation();
            return new AcceptLogIn(authInfo);

        }
        return this.handleRejectedLogin('invalid token');
    }


}
