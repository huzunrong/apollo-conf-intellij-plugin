package io.github.newhoo.apollo;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiShortNamesCache;
import io.github.newhoo.apollo.setting.PluginGlobalSetting;
import io.github.newhoo.apollo.util.NotificationUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Optional;

/**
 * ApolloOpenInBrowserAction
 *
 * @author huzunrong
 * @since 1.0
 */
public class ApolloOpenInBrowserAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().setEnabledAndVisible(StringUtils.isNotBlank(findApolloAppId(project)));
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null || project.isDefault()) {
            return;
        }

        String appId = findApolloAppId(project);
        if (StringUtils.isEmpty(appId)) {
            NotificationUtils.errorBalloon(project, "Cannot Find Apollo AppId", "");
            return;
        }

        String apolloBaseUrl = PluginGlobalSetting.getInstance().getApolloBaseUrl();
        if (StringUtils.isEmpty(apolloBaseUrl)) {
            NotificationUtils.errorBalloon(project, "Apollo Host not Configured", "");
            return;
        }

        String apolloUrl = apolloBaseUrl + "/config.html?#/appid=" + appId;
        if (!apolloUrl.startsWith("http")) {
            apolloUrl = "http://" + apolloUrl;
        }
        if (StringUtils.isNoneEmpty(apolloUrl)) {
            CopyPasteManager.getInstance().setContents(new StringSelection(apolloUrl));
        }
        BrowserUtil.browse(apolloUrl);
    }

    private String findApolloAppId(Project project) {
        return findProjectProperty(project, "app.id", "app.properties", "META-INF");
    }

    public static String findProjectProperty(Project project, String key, @NotNull String fileName, String parentName) {
        VirtualFile propertiesFile = findProjectFile(project, fileName, parentName);
        if (propertiesFile != null) {

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(propertiesFile.getInputStream(), Charset.forName("UTF-8")))) {

                Optional<String> first = br.lines()
                                           .filter(line -> !line.startsWith("#"))
                                           .filter(line -> line.startsWith(key + "=") || line.startsWith(key + " ="))
                                           .findFirst();
                if (first.isPresent()) {
                    String[] split = StringUtils.split(first.get(), '=');
                    if (ArrayUtils.isNotEmpty(split) && split.length > 1 && StringUtils.isNoneEmpty(split[1])) {
                        return split[1];
                    }
                }
            } catch (IOException ex) {
                NotificationUtils.errorBalloon(project, String.format("Cannot Find Property %s in %s", key, fileName), ex);
            }
        }
        return null;
    }

    public static VirtualFile findProjectFile(Project project, @NotNull String fileName, String parentName) {
        PsiFile[] filesByName = PsiShortNamesCache.getInstance(project).getFilesByName(fileName);
        if (ArrayUtils.isEmpty(filesByName)) {
            return null;
        }
        if (StringUtils.isEmpty(parentName)) {
            return filesByName[0].getVirtualFile();
        }
        for (PsiFile psiFile : filesByName) {
            VirtualFile virtualFile = psiFile.getVirtualFile();
            if (virtualFile != null && virtualFile.getParent() != null && parentName.equals(virtualFile.getParent().getName())) {
                return virtualFile;
            }
        }
        return null;
    }
}