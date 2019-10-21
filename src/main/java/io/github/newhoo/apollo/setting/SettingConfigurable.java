package io.github.newhoo.apollo.setting;

import com.intellij.openapi.options.Configurable;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * SettingConfigurable
 *
 * @author huzunrong
 * @since 1.0
 */
public class SettingConfigurable implements Configurable {

    private final PluginGlobalSetting globalSetting = PluginGlobalSetting.getInstance();
    private final SettingForm settingForm;

    public SettingConfigurable() {
        this.settingForm = new SettingForm();
    }

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Apollo Conf";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        reset();

        return settingForm.mainPanel;
    }

    @Override
    public boolean isModified() {
        return !StringUtils.equals(globalSetting.getApolloBaseUrl(), settingForm.apolloBaseUrlText.getText());
    }

    @Override
    public void apply() {
        globalSetting.setApolloBaseUrl(settingForm.apolloBaseUrlText.getText());
    }

    @Override
    public void reset() {
        settingForm.apolloBaseUrlText.setText(globalSetting.getApolloBaseUrl());
    }
}