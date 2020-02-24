unit RecordHelperConstant;

{This is a sample Delphi file.}

interface

type
  TExtendedHelper = record helper for Extended
  public
    const
      PositiveInfinity: Extended =  1.0 / 0.0;
  end;

implementation

procedure Test(Foo: Extended);
begin
  Test(Foo.PositiveInfinity);
  Test(Extended.PositiveInfinity);
end;

end.