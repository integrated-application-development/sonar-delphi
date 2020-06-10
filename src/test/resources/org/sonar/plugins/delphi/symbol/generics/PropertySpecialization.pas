unit PropertySpecialization;

{This is a sample Delphi file.}

interface

 type
   TFoo<T> = class
     FData: T;
     property Data: T read FData;
   end;

implementation

procedure ConsumeString(Arg: String);
begin
  // Do nothing
end;

procedure Test(Foo: TFoo<String>);
begin
  ConsumeString(Foo.Data);
end;

end.