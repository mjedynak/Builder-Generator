package pl.mjedynak.idea.plugins.builder.action.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import javax.swing.JList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.mjedynak.idea.plugins.builder.factory.GoToBuilderPopupListFactory;
import pl.mjedynak.idea.plugins.builder.finder.BuilderFinder;
import pl.mjedynak.idea.plugins.builder.gui.displayer.GoToBuilderPopupDisplayer;
import pl.mjedynak.idea.plugins.builder.psi.PsiHelper;
import pl.mjedynak.idea.plugins.builder.verifier.BuilderVerifier;

@ExtendWith(MockitoExtension.class)
public class GoToBuilderActionHandlerTest {

    @InjectMocks
    private GoToBuilderActionHandler builderActionHandler;

    @Mock
    private BuilderVerifier builderVerifier;

    @Mock
    private BuilderFinder builderFinder;

    @Mock
    private PsiHelper psiHelper;

    @Mock
    private GoToBuilderPopupListFactory popupListFactory;

    @Mock
    private GoToBuilderPopupDisplayer popupDisplayer;

    @Mock
    private DisplayChoosers displayChoosers;

    @Mock
    private PsiClass psiClass;

    @Mock
    private PsiClass builderClass;

    @Mock
    private Editor editor;

    @Mock
    private Project project;

    @Mock
    private DataContext dataContext;

    @SuppressWarnings("rawtypes")
    @Mock
    private JList list;

    @BeforeEach
    public void setUp() {
        given(dataContext.getData(CommonDataKeys.PROJECT.getName())).willReturn(project);
    }

    @Test
    void shouldNavigateToBuilderIfItExistsAndInvokedInsideNotBuilderClass() {
        // given
        given(psiHelper.getPsiClassFromEditor(editor, project)).willReturn(psiClass);
        given(builderVerifier.isBuilder(psiClass)).willReturn(false);
        given(builderFinder.findBuilderForClass(psiClass)).willReturn(builderClass);

        // when
        builderActionHandler.execute(editor, dataContext);

        // then
        verify(psiHelper).navigateToClass(builderClass);
    }

    @Test
    void shouldNavigateToNotBuilderClassIfItExistsAndInvokedInsideBuilder() {
        // given
        given(psiHelper.getPsiClassFromEditor(editor, project)).willReturn(builderClass);
        given(builderVerifier.isBuilder(builderClass)).willReturn(true);
        given(builderFinder.findClassForBuilder(builderClass)).willReturn(psiClass);

        // when
        builderActionHandler.execute(editor, dataContext);

        // then
        verify(psiHelper).navigateToClass(psiClass);
    }

    @Test
    void shouldDisplayPopupWhenBuilderNotFoundAndInvokedInsideNotBuilderClass() {
        // given
        given(psiHelper.getPsiClassFromEditor(editor, project)).willReturn(psiClass);
        given(builderVerifier.isBuilder(psiClass)).willReturn(false);
        given(builderFinder.findBuilderForClass(psiClass)).willReturn(null);
        given(popupListFactory.getPopupList()).willReturn(list);

        // when
        builderActionHandler.execute(editor, dataContext);

        // then
        verifyDisplayChoosersSetMethods();
        ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(popupDisplayer).displayPopupChooser(eq(editor), eq(list), runnableArgumentCaptor.capture());
        runnableArgumentCaptor.getValue().run();
        verify(displayChoosers).run(null);
    }

    private void verifyDisplayChoosersSetMethods() {
        verify(displayChoosers).setEditor(editor);
        verify(displayChoosers).setProject(project);
        verify(displayChoosers).setPsiClassFromEditor(psiClass);
    }

    @Test
    void shouldNotDoAnythingWhenNotBuilderClassNotFoundAndInvokedInsideBuilder() {
        // given
        given(psiHelper.getPsiClassFromEditor(editor, project)).willReturn(builderClass);
        given(builderVerifier.isBuilder(builderClass)).willReturn(true);
        given(builderFinder.findClassForBuilder(builderClass)).willReturn(null);

        // when
        builderActionHandler.execute(editor, dataContext);

        // then
        verifyNothingIsDone();
    }

    private void verifyNothingIsDone() {
        verify(psiHelper, never()).navigateToClass(any(PsiClass.class));
        verify(displayChoosers, never()).run(any(PsiClass.class));
        verifyNoMoreInteractions(popupDisplayer);
    }
}
