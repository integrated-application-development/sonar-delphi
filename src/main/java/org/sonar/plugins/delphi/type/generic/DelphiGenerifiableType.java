package org.sonar.plugins.delphi.type.generic;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

/**
 * Types that can be declared as generics are considered generifiable.
 *
 * <p>According to Embarcadero, the following types fit into this category:
 *
 * <ul>
 *   <li>classes
 *   <li>interfaces
 *   <li>records
 *   <li>arrays
 *   <li>procedural types
 * </ul>
 *
 * @see <a href="http://docwiki.embarcadero.com/RADStudio/Rio/en/Declaring_Generics">Declaring
 *     Generics</a>
 */
public abstract class DelphiGenerifiableType extends DelphiType {
  private final Map<TypeSpecializationContext, DelphiGenerifiableType> cache;

  protected DelphiGenerifiableType() {
    cache = new HashMap<>();
  }

  /**
   * If applicable, creates a new type with any relevant generic types specialized.
   *
   * @param context information about the type arguments and parameters
   * @return specialized type
   */
  @Override
  public final Type specialize(TypeSpecializationContext context) {
    if (context.hasSignatureMismatch() || !canBeSpecialized(context)) {
      return this;
    }

    DelphiGenerifiableType result = cache.get(context);
    if (result == null) {
      result = this.doSpecialization(context);
      if (result.is(this)) {
        result = this;
      } else {
        cache.put(context, result);
        result.doAfterSpecialization(context);
      }
    }

    return result;
  }

  /**
   * Called on the generic type. This is where we actually do the work to specialize a type.
   *
   * @param context Contains data about the type parameters and arguments for specialization
   * @return the specialized type, or unknown type if generic specialization fails
   * @see Type#specialize(TypeSpecializationContext)
   */
  protected abstract DelphiGenerifiableType doSpecialization(TypeSpecializationContext context);

  /**
   * This method is called on the specialized type after it has been instantiated and cached.
   *
   * <p>Helpful for breaking cyclic dependencies. <br>
   * (Example: A struct type has a type scope which can contain fields with the type specialization
   * we are currently generating. Therefore, we need to specialize the type scope <i>after</i> the
   * type is instantiated.)
   *
   * @param context Contains data about the type parameters and arguments for specialization
   * @see Type#specialize(TypeSpecializationContext)
   */
  protected void doAfterSpecialization(TypeSpecializationContext context) {
    // Do nothing
  }
}
