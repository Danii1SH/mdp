function togglePassword(inputId, icon) {
    const passwordInput = document.getElementById(inputId);
    const isPasswordVisible = passwordInput.type === "text";

    passwordInput.type = isPasswordVisible ? "password" : "text";

    const newIconSrc = isPasswordVisible ? "/img/form_icon/show-password.png" : "/img/form_icon/hide-password.png";
    const newIconAlt = isPasswordVisible ? "Показать пароль" : "Скрыть пароль";

    icon.src = newIconSrc;
    icon.alt = newIconAlt;
}
