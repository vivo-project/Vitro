<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for notifying user about reset password request state -->

<#if showPasswordChangeForm == true>
    <h1>${i18n().password_reset_title}</h1>
    <br/>

    <form id="forgotPasswordForm" action="${forgotPasswordUrl}" method="POST" style="margin-top: 10px;">
        <div>
            <label for="email">${i18n().password_reset_manual}</label>
            <input type="email" id="email" name="email" class="text-field focus" placeholder="user@example.com" required>

            <p class="submit" style="margin-top: 10px;">
                <button type="submit" class="green button">${i18n().password_reset_button}</button>
            </p>
        </div>
    </form>
<#else>
    <h1>${message}</h1>
</#if>

<style>
    label[for="email"] {
        font-size: 18px;
        display: block;
        margin-bottom: 5px;
    }

    input#email {
        font-size: 16px;
        height: 1.5em;
        width: 20em;
    }

    .submit button.green {
        font-size: 16px;
    }

    p {
        font-size: 16px;
    }

    a {
        color: #0072b5; /* Adjust link color as needed */
        text-decoration: none;
    }

    h1 {
        font-size: 24px;
    }
</style>

