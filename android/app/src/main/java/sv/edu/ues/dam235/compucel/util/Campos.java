package sv.edu.ues.dam235.compucel.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;

import sv.edu.ues.dam235.compucel.R;

/** Ayudas para los campos de texto con el estilo de error del diseno (icono de alerta antes del mensaje). */
public final class Campos {

    private Campos() {
    }

    public static String texto(TextInputLayout campo) {
        return campo.getEditText() == null || campo.getEditText().getText() == null
                ? "" : campo.getEditText().getText().toString();
    }

    public static void mostrarError(TextInputLayout campo, @StringRes int mensaje) {
        Context context = campo.getContext();
        Drawable icono = ContextCompat.getDrawable(context, R.drawable.ic_warning);
        SpannableString texto = new SpannableString("  " + context.getString(mensaje));
        if (icono != null) {
            int lado = Math.round(16 * context.getResources().getDisplayMetrics().density);
            icono.setBounds(0, 0, lado, lado);
            texto.setSpan(new ImageSpan(icono, ImageSpan.ALIGN_CENTER), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        campo.setErrorIconDrawable(null);
        campo.setEndIconTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.error_texto_campo)));
        campo.setError(texto);
    }

    /** Quita el error del campo en cuanto el usuario vuelve a escribir. */
    public static void limpiarAlEscribir(TextInputLayout... campos) {
        for (TextInputLayout campo : campos) {
            if (campo.getEditText() == null) {
                continue;
            }
            campo.getEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (campo.getError() != null) {
                        campo.setError(null);
                        campo.setEndIconTintList(ColorStateList.valueOf(
                                ContextCompat.getColor(campo.getContext(), R.color.on_surface_variant)));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
}
