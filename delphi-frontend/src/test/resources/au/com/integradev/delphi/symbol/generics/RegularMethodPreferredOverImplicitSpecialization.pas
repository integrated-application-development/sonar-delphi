unit RegularMethodPreferredOverImplicitSpecialization;

// Example from the Embarcadero wiki
// See: https://docwiki.embarcadero.com/RADStudio/Rio/en/Overloads_and_Type_Compatibility_in_Generics

interface

 type
   TFoo = class
     procedure Proc<T>(A: T); overload;
     procedure Proc(A: String); overload;
     procedure Test;
   end;

implementation

procedure TFoo.Test;
begin
  Proc('Hello'); // calls Proc(A: String);
  Proc<String>('Hello'); // calls Proc<T>(A: T);
end;

end.