unit TypeAliasParameter;

{This is a sample Delphi file.}

interface

type
  TBar = class(TObject)
  end;

  TBarAlias = TBar;

  TFoo = class(TObject)
  public
    procedure Bar(Param: TBar);
  end;

implementation

procedure TFoo.Bar(AliasParam: TBarAlias);
begin
  // Do nothing
end;

end.