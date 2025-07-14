unit ArrayAssignmentCompatibility;

// Example from the Embarcadero wiki
// See: https://docwiki.embarcadero.com/RADStudio/Rio/en/Overloads_and_Type_Compatibility_in_Generics

interface

 type
   TArray<T> = array of T;
   
   TFoo = class
     procedure Proc(Foo: TArray<String>); overload;
     procedure Proc(Bar: TArray<ShortString>); overload;
     procedure Proc(Baz: TArray<Integer>); overload;
     procedure Proc(Xyzzyz: TArray<Boolean>); overload;
     procedure Test;
   end;

implementation

procedure TFoo.Test;
var
  ShortStr: String[5];
begin
  Proc(TArray<String>.Create('foo'));
  Proc(TArray<ShortString>.Create(ShortStr));
  Proc(TArray<Integer>.Create(1, 2, 3));
  Proc(TArray<Boolean>.Create(True, False));
end;

end.