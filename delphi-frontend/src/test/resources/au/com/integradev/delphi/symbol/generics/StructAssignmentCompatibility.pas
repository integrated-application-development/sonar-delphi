unit StructAssignmentCompatibility;

// Example from the Embarcadero wiki
// See: https://docwiki.embarcadero.com/RADStudio/Rio/en/Overloads_and_Type_Compatibility_in_Generics

interface

 type
   TFoo<T> = class
    constructor Create(Arg: T);
   end;
   
   TFoo = class
     procedure Proc(Foo: TFoo<String>); overload;
     procedure Proc(Bar: TFoo<ShortString>); overload;
     procedure Proc(Baz: TFoo<Integer>); overload;
     procedure Proc(Xyzzyz: TFoo<Boolean>); overload;
     procedure Test;
   end;

implementation

procedure TFoo.Test;
var
  ShortStr: String[5];
begin
  Proc(TFoo<String>.Create('foo'));
  Proc(TFoo<ShortString>.Create(ShortStr));
  Proc(TFoo<Integer>.Create(1));
  Proc(TFoo<Boolean>.Create(True));
end;

end.